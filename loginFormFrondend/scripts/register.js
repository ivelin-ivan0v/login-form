function registerUser() {
  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;
  const firstName = document.getElementById("firstName").value;
  const lastName = document.getElementById("lastName").value;

  resetErrorMessages();

  if (!isValidEmail(email)) {
    displayError("email", "Invalid email format");
    return false;
  }

  if (password.length < 8) {
    displayError("password", "Password must be at least 8 characters");
    return false;
  }

  if (firstName.length === 0) {
    displayError("firstName", "First name is required");
    return false;
  }

  if (lastName.length === 0) {
    displayError("lastName", "Last name is required");
    return false;
  }

  fetch("http://localhost:8000/users", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ email, firstName, lastName, password }),
  })
    .then((response) => response.json())
    .then((data) => {
      console.log("Registration successful:", data);
      localStorage.setItem("userEmail", email);
      window.location.href = "userPage.html";
    })
    .catch((error) => {
      console.error("Registration failed:", error);
    });
}

function isValidEmail(email) {
  // Simple email validation regex
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
