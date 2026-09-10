function myFunction() {
  var x = document.getElementById("myInput");
  if (x.type == "password") {
    x.type = "text";
  } else {
    x.type = "password";
  }
}

function myfunctionconfirm()
{
  var x =document.getElementById("myInputuser");
  if(x.type == "password")
  {
    x.type ="text";
  }
  else{
    x.type ="password";
  }
}
function exception(){
  var pass = document.getElementById("myInput").value;
  var confirm = document.getElementById("myInputuser").value;
  var confirmpass=document.getElementById("myInputuser");
  var error = document.getElementById("errormessage");
  if(confirm.length >0)
  {
      if(pass!=confirm)
      {
         confirmpass.style.border
      }
  }
}