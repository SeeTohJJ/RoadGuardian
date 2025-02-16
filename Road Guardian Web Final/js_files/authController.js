class AuthController{
  failCount = 0;
  timeOut = 0;

  static startCountdown(seconds) {
    AuthController.timeOut = seconds;
    const intervalId = setInterval(() => {
      AuthController.timeOut--;
        
        if (AuthController.timeOut < 0) {
            clearInterval(intervalId);
            AuthController.failCount = 0;
        }
    }, 1000);
  }

  static initSubmit() {
    const fullPath = window.location.href;
    const link = fullPath.substring(fullPath.lastIndexOf('/') + 1);

    let password;
    AuthController.failCount = 0;
    AuthController.timeOut = 0;

    document.getElementById('form').addEventListener('submit', async function(event) {
      event.preventDefault();
      // Read email and password fields
      const email = document.getElementById('email').value;
      
      switch (link){
        // Login Page
        case 'loginUI.html':
          password = document.getElementById('password').value;
          const loginObj = ObjectFactory.createAuth('loginButton', email, password);
          const approve = Boolean(await loginObj.get_data());
          
          if (AuthController.timeOut>0){
            alert(`Please try again in ${AuthController.timeOut%60} seconds. Alternatively select 'Reset Password'`)
            return;
          }
      
          if (approve){
            alert('Welcome: ' + email);
            window.location.href = "dashboardUI.html";  // Redirect to dashboard
            AuthController.failCount = 0;
          }
          else if (AuthController.failCount < 4){
            AuthController.failCount = AuthController.failCount + 1;
            alert("Invalid Login Credentials - " + AuthController.failCount);
          }
          else{
            alert("Please try again in 1 minute. Alternatively select 'Reset Password'")
            AuthController.startCountdown(60);
          }

          break;
        
        // Register Button
        case 'registerUI.html':
          password = document.getElementById('password').value;
          const regObj = ObjectFactory.createAuth('registerButton', email, password);
          const registered = Boolean(await regObj.update());

          if (registered){
            alert("Registered, Please Login");
          }
          else{
            alert("Email Exists");
          }

          break;
        
        // Reset Button
        case 'resetUI.html':
          const resetObj = ObjectFactory.createAuth('resetButton', email, null);
          const resetted = Boolean(await resetObj.update());

          if (resetted){
            alert("Please check inbox");
          }
          else{
            alert("Account doesn't exist");
          }
          break;

        default:
            alert("Page not supported. Please close browser.");
      }
    });
  }
}
