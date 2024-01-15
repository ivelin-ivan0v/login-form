function loginUser() {
  const email = document.getElementById("loginEmail").value;
  const password = document.getElementById("loginPassword").value;

  resetErrorMessages();

  if (!isValidEmail(email)) {
    displayError("email", "Invalid email format");
    return false;
  }

  if (password.length < 8) {
    displayError("password", "Password must be at least 8 characters");
    return false;
  }

  fetch("http://localhost:8000/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ email, password }),
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.error) {
        console.log("Login unsuccessfull:", data);
        window.alert("Wrong email or password");
      } else {
        console.log("Login successful:", data);
        localStorage.setItem("userEmail", email);
        window.location.href = "userPage.html";
      }
    })
    .catch((error) => {
      console.error("Login failed:", error);
    });
}

function redirectToRegister() {
  window.location.href = "register.html";
}

function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

function displayError(fieldId, errorMessage) {
  const errorSpan = document.getElementById(`${fieldId}Error`);
  errorSpan.innerText = errorMessage;
}

function resetErrorMessages() {
  const errorSpans = document.querySelectorAll(".error");
  errorSpans.forEach((span) => (span.innerText = ""));
}
