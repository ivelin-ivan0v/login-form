let currentEmail = localStorage.getItem("userEmail");

if (currentEmail) {
  document.addEventListener("DOMContentLoaded", getUserInfo());
} else {
  window.alert("Invalid email or password!");
}

function getUserInfo() {
  fetch(`http://localhost:8000/users?email=${currentEmail}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then((response) => response.json())
    .then((data) => {
      const userInfoContainer = document.getElementById("userInfo");
      userInfoContainer.innerHTML = `
                        <p>Email: ${data.user.email}</p>
                        <p>First Name: ${data.user.first_name}</p>
                        <p>Last Name: ${data.user.last_name}</p>
                    `;
    })
    .catch((error) => {
      console.error("Failed to fetch user information:", error);
      window.alert("Wrong email or password!");
      window.location.href = "login.html";
    });
}

function updateUser() {
    window.location.href = "updateUser.html";
}

function deleteUser() {
  if (window.confirm("Are you sure you want to DELETE the user?")) {
    fetch(`http://localhost:8000/users?email=${currentEmail}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
      },
    })
      .then((response) => response.json())
      .then((data) => {
        console.log("User account deleted:", data);
        window.location.href = "login.html";
      })
      .catch((error) => {
        // Handle delete error
        console.error("Failed to delete user account:", error);
      });
  }
}

function logout() {
  if (window.confirm("Are you sure you want to log out?")) {
    window.location.href = "login.html";
  }
}
