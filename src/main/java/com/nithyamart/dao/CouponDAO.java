package com.nithyamart.dao;

import com.nithyamart.model.Coupon;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CouponDAO {

    private final DataSource dataSource;

    public CouponDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Coupon findByCode(String code) throws SQLException {
        if (code == null || code.isBlank()) {
            return null;
        }

        String sql = """
                SELECT id, code, discount_type, discount_value, min_order_value, max_discount, valid_until, is_active, created_at
                FROM coupons
                WHERE UPPER(code) = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code.trim().toUpperCase());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapCoupon(rs);
                }
            }
        }
        return null;
    }

    public List<Coupon> findAllActive() throws SQLException {
        String sql = """
                SELECT id, code, discount_type, discount_value, min_order_value, max_discount, valid_until, is_active, created_at
                FROM coupons
                WHERE is_active = TRUE
                ORDER BY discount_value DESC
                """;

        List<Coupon> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Coupon c = mapCoupon(rs);
                if (c.getValidUntil() == null || c.getValidUntil().isAfter(LocalDateTime.now())) {
                    list.add(c);
                }
            }
        }
        return list;
    }

    public List<Coupon> findAll() throws SQLException {
        String sql = """
                SELECT id, code, discount_type, discount_value, min_order_value, max_discount, valid_until, is_active, created_at
                FROM coupons
                ORDER BY id DESC
                """;

        List<Coupon> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                list.add(mapCoupon(rs));
            }
        }
        return list;
    }

    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon == null || !coupon.isActive() || subtotal == null) {
            return BigDecimal.ZERO;
        }

        if (coupon.getValidUntil() != null && coupon.getValidUntil().isBefore(LocalDateTime.now())) {
            return BigDecimal.ZERO;
        }

        if (coupon.getMinOrderValue() != null && subtotal.compareTo(coupon.getMinOrderValue()) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;
        if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = subtotal.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscount() != null && coupon.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0) {
                if (discount.compareTo(coupon.getMaxDiscount()) > 0) {
                    discount = coupon.getMaxDiscount();
                }
            }
        } else if ("FLAT".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = coupon.getDiscountValue();
        }

        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }
        return discount;
    }

    private Coupon mapCoupon(ResultSet rs) throws SQLException {
        Timestamp validUntilTs = rs.getTimestamp("valid_until");
        Timestamp createdTs = rs.getTimestamp("created_at");

        return new Coupon(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("discount_type"),
                rs.getBigDecimal("discount_value"),
                rs.getBigDecimal("min_order_value"),
                rs.getBigDecimal("max_discount"),
                validUntilTs != null ? validUntilTs.toLocalDateTime() : null,
                rs.getBoolean("is_active"),
                createdTs != null ? createdTs.toLocalDateTime() : null
        );
    }
}
