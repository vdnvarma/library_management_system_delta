const API_BASE = process.env.REACT_APP_API_URL || "http://localhost:8080/api";
console.log("API_BASE URL:", API_BASE);

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
  
  // Update this to use the correct backend URL
  const urls = [
    `${API_BASE}/users/login`,
    `${API_BASE}/api/users/login`,
    // Update this URL to your actual backend URL
    `https://library-management-system-backend-lms-demo.onrender.com/api/users/login`
  ];
  
  let lastError = null;
  let lastStatus = null;
  
  // Try each URL until one works
  for (const url of urls) {
    console.log("Trying login URL:", url);
    
    try {
      const res = await fetch(url, {
        method: "POST",
        headers: { 
          "Content-Type": "application/json",
          // Add this to help with debugging CORS
          "X-Requested-With": "XMLHttpRequest"
        },
        body: JSON.stringify({ username, password }),
        // Explicitly set credentials mode to omit cookies
        // which can help with CORS issues
        credentials: 'omit'
      });
      
      console.log(`Login response from ${url}: status ${res.status}`);
      
      if (!res.ok) {
        console.log("Login attempt failed with status: " + res.status);
        lastStatus = res.status;
        try {
          const errorData = await res.json();
          console.log("Error response:", errorData);
          lastError = { error: errorData.error || "Login failed", status: res.status };
          // Continue to next URL
        } catch (e) {
          lastError = { error: "Login failed", status: res.status };
          // Continue to next URL
        }
      } else {
        // Success! Return the data
        const data = await res.json();
        console.log("Login successful, token received: " + (data.token ? "yes" : "no"));
        return data;
      }
    } catch (error) {
      console.error(`Login request to ${url} error:`, error);
      lastError = { error: "Network or CORS error", details: error.message };
      // Continue to next URL
    }
  }
  
  // If we get here, all URLs failed
  console.log("All login attempts failed");
  
  // If we got a 401 Unauthorized error, suggest trying the test page
  if (lastStatus === 401) {
    return { 
      error: "Login failed: Username or password incorrect. Use the test page to create or reset your admin user: https://library-management-system-backend-lms-demo.onrender.com/test.html",
      status: lastStatus
    };
  }
  
  // If we got a different error or no response, it might be a CORS or connectivity issue
  return lastError || { error: "Login failed. If this persists, try the test page: https://library-management-system-backend-lms-demo.onrender.com/test.html" };
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