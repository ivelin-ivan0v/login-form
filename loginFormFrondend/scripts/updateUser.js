function updateUser() {
  const firstName = document.getElementById("firstName").value;
  const lastName = document.getElementById("lastName").value;
  let currentEmail = localStorage.getItem("userEmail");

  resetErrorMessages();

    if (firstName.length === 0) {
        displayError('firstName', 'First name is required');
        return false;
    }

    if (lastName.length === 0) {
        displayError('lastName', 'Last name is required');
        return false;
    }

  fetch(`http://localhost:8000/users?email=${currentEmail}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ firstName, lastName}),
  })
    .then((response) => response.json())
    .then((data) => {
      console.log("User updated successfully:", data);
      window.location.href = "userPage.html";
    })
    .catch((error) => {
      console.error("Registration failed:", error);
    });
}

function displayError(fieldId, errorMessage) {
    const errorSpan = document.getElementById(`${fieldId}Error`);
    errorSpan.innerText = errorMessage;
}

function resetErrorMessages() {
    const errorSpans = document.querySelectorAll('.error');
    errorSpans.forEach(span => span.innerText = '');
}
