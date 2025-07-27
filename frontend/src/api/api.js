const API_BASE = process.env.REACT_APP_API_URL || "http://localhost:8080/api";

// JWT helpers
export function setJwt(token) {
  localStorage.setItem("jwt", token);
  console.log("JWT token stored: " + (token ? "yes" : "no"));
}

export function getJwt() {
  const token = localStorage.getItem("jwt");
  console.log("JWT token retrieved: " + (token ? "yes" : "no"));
  return token;
}

// Public endpoints
export async function login(username, password) {
  console.log("Attempting login for user: " + username);
  const res = await fetch(`${API_BASE}/users/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  if (!res.ok) {
    console.log("Login failed with status: " + res.status);
    return { error: "Login failed", status: res.status };
  }
  const data = await res.json();
  console.log("Login successful, token received: " + (data.token ? "yes" : "no"));
  return data;
}

export async function register(name, username, password) {
  const res = await fetch(`${API_BASE}/users/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, username, password }),
  });
  if (!res.ok) {
    return { error: "Registration failed", status: res.status };
  }
  return res.json();
}

// Protected endpoints (require JWT)
function authHeaders() {
  const token = getJwt();
  const headers = token ? { "Authorization": "Bearer " + token } : {};
  console.log("Authorization header included: " + (headers.Authorization ? "yes" : "no"));
  return headers;
}

export async function getBooks() {
  const res = await fetch(`${API_BASE}/books`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
}

export async function searchBooks(type, keyword) {
  const res = await fetch(`${API_BASE}/books/search?type=${type}&keyword=${keyword}`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
}

export async function addBook(book) {
  const res = await fetch(`${API_BASE}/books`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...authHeaders()
    },
    body: JSON.stringify(book),
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
}

export async function deleteBook(id) {
  const res = await fetch(`${API_BASE}/books/${id}`, {
    method: "DELETE",
    headers: authHeaders()
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res;
}

// Add similar JWT logic for other protected endpoints as needed
// Example for issueBook, returnBook, reserveBook, etc.
export async function issueBook(bookId, userId) {
  const res = await fetch(`${API_BASE}/issues/issue?bookId=${bookId}&userId=${userId}`, {
    method: "POST",
    headers: authHeaders()
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
}

export async function returnBook(issueId, finePaid = null) {
  try {
    let url = `${API_BASE}/issues/return?issueId=${issueId}`;
    if (finePaid !== null) {
      url += `&finePaid=${finePaid}`;
    }
    
    const res = await fetch(url, {
      method: "POST",
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      throw new Error(errorData.error || `Error ${res.status}: Unable to return book`);
    }
    return res.json();
  } catch (err) {
    console.error("Error returning book:", err);
    throw err;
  }
}

export async function reserveBook(bookId, userId) {
  const res = await fetch(`${API_BASE}/reservations/reserve?bookId=${bookId}&userId=${userId}`, {
    method: "POST",
    headers: authHeaders()
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
}

export async function getUserIssues(userId) {
  try {
    const res = await fetch(`${API_BASE}/issues/user/${userId}`, {
      headers: authHeaders()
    });
    if (!res.ok) {
      console.warn(`Failed to get user issues: ${res.status}`);
      return [];
    }
    const data = await res.json();
    return Array.isArray(data) ? data : [];
  } catch (err) {
    console.error("Error fetching user issues:", err);
    return [];
  }
}

export async function getUserReservations(userId) {
  const res = await fetch(`${API_BASE}/reservations/user/${userId}`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
}

// Reports and analytics
export async function getMostIssuedBooks() {
  try {
    const res = await fetch(`${API_BASE}/issues/reports/mostIssued`, {
      headers: authHeaders()
    });
    if (!res.ok) {
      console.warn(`Failed to fetch popular books: ${res.status} ${res.statusText}`);
      // Return empty array for any error
      return [];
    }
    const data = await res.json();
    return Array.isArray(data) ? data : [];
  } catch (err) {
    console.error("Error fetching popular books:", err);
    return [];
  }
}

export async function getUserActivityReport(userId) {
  const res = await fetch(`${API_BASE}/issues/reports/userActivity/${userId}`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
}

export async function getFinesReport() {
  const res = await fetch(`${API_BASE}/issues/reports/fines`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
}

// User management for admins
export async function getAllUsers() {
  const res = await fetch(`${API_BASE}/users`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
}

export async function updateUserRole(userId, role) {
  const res = await fetch(`${API_BASE}/users/${userId}/role`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      ...authHeaders()
    },
    body: JSON.stringify({ role }),
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
}

export async function getUsersByRole(role) {
  const res = await fetch(`${API_BASE}/users/byRole/${role}`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
}

export async function deleteUser(userId) {
  const res = await fetch(`${API_BASE}/users/${userId}`, {
    method: "DELETE",
    headers: authHeaders()
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
}

// Issue management
export async function getAllIssues() {
  const res = await fetch(`${API_BASE}/issues`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
}

export async function calculateFine(issueId) {
  try {
    const res = await fetch(`${API_BASE}/issues/fine/${issueId}`, {
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      throw new Error(errorData.error || `Error ${res.status}: Unable to calculate fine`);
    }
    return res.json();
  } catch (err) {
    console.error("Error calculating fine:", err);
    throw err;
  }
}

// Reservation management
export async function getAllReservations() {
  const res = await fetch(`${API_BASE}/reservations`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
}

export async function cancelReservation(reservationId) {
  const res = await fetch(`${API_BASE}/reservations/${reservationId}`, {
    method: "DELETE",
    headers: authHeaders()
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
}

// Reservation notifications
export async function notifyReservation(reservationId) {
  const res = await fetch(`${API_BASE}/reservations/notify-available/${reservationId}`, {
    method: "POST",
    headers: authHeaders()
  });
  if (!res.ok) {
    return { error: "Unauthorized", status: res.status };
  }
  return res.json();
} 