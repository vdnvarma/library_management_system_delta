const API_BASE = process.env.REACT_APP_API_URL || "http://localhost:8080";
const API_PREFIX = "/api";

console.log("API_BASE URL:", API_BASE);

// Helper to fetch from multiple URL variations (with and without /api prefix)
async function fetchWithFallback(endpoint, options = {}) {
  // Try both with and without /api prefix
  const urls = [
    `${API_BASE}${endpoint}`,                // No prefix (using the LegacyUrlController)
    `${API_BASE}${API_PREFIX}${endpoint}`,   // With /api prefix (original controllers)
  ];
  
  let lastError = null;
  let lastStatus = null;
  
  console.log(`Attempting request to endpoint: ${endpoint}`);
  
  // Try each URL until one works
  for (const url of urls) {
    console.log(`Trying URL: ${url}`);
    
    try {
      const res = await fetch(url, options);
      console.log(`Response from ${url}: status ${res.status}`);
      
      if (!res.ok) {
        console.log(`Request failed with status: ${res.status}`);
        lastStatus = res.status;
        try {
          const errorData = await res.json();
          console.log("Error response:", errorData);
          lastError = { error: errorData.error || `Request failed (${res.status})`, status: res.status };
          // Continue to next URL
        } catch (e) {
          lastError = { error: `Request failed (${res.status})`, status: res.status };
          // Continue to next URL
        }
      } else {
        // Success! Return the response directly
        return res;
      }
    } catch (error) {
      console.error(`Request to ${url} failed:`, error);
      lastError = { error: "Network or CORS error", details: error.message };
      // Continue to next URL
    }
  }
  
  // If we get here, all URLs failed
  console.log("All URL attempts failed");
  
  // Create a Response object with the error
  const errorResponse = new Response(JSON.stringify(lastError || { error: "Request failed" }), {
    status: lastStatus || 500,
    headers: { 'Content-Type': 'application/json' }
  });
  
  return errorResponse;
}

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
  
  try {
    // Special case for login - try the demo server URL as well
    const urls = [
      `${API_BASE}/users/login`,  // Legacy URL
      `${API_BASE}${API_PREFIX}/users/login`, // With API prefix
      `https://library-management-system-backend-lms-demo.onrender.com/api/users/login` // Demo server
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
            "X-Requested-With": "XMLHttpRequest"
          },
          body: JSON.stringify({ username, password }),
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
          } catch (e) {
            lastError = { error: "Login failed", status: res.status };
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
  } catch (error) {
    console.error("Unexpected error during login:", error);
    return { error: "An unexpected error occurred during login" };
  }
}

export async function register(name, username, password) {
  try {
    const res = await fetchWithFallback("/users/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name, username, password }),
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Registration failed" }));
      return { error: errorData.error || "Registration failed", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error during registration:", error);
    return { error: "Registration failed due to network error" };
  }
}

// Protected endpoints (require JWT)
function authHeaders() {
  const token = getJwt();
  const headers = token ? { "Authorization": "Bearer " + token } : {};
  console.log("Authorization header included: " + (headers.Authorization ? "yes" : "no"));
  return headers;
}

export async function getBooks() {
  try {
    const res = await fetchWithFallback("/books", {
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to get books", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error fetching books:", error);
    return { error: "Network error while fetching books", details: error.message };
  }
}

export async function searchBooks(type, keyword) {
  try {
    const res = await fetchWithFallback(`/books/search?type=${type}&keyword=${keyword}`, {
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Search failed", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error searching books:", error);
    return { error: "Network error while searching", details: error.message };
  }
}

export async function addBook(book) {
  try {
    const res = await fetchWithFallback("/books", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...authHeaders()
      },
      body: JSON.stringify(book),
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to add book", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error adding book:", error);
    return { error: "Network error while adding book", details: error.message };
  }
}

export async function deleteBook(id) {
  try {
    const res = await fetchWithFallback(`/books/${id}`, {
      method: "DELETE",
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to delete book", status: res.status };
    }
    return res;
  } catch (error) {
    console.error("Error deleting book:", error);
    return { error: "Network error while deleting book", details: error.message };
  }
}

// Add similar JWT logic for other protected endpoints as needed
// Example for issueBook, returnBook, reserveBook, etc.
export async function issueBook(bookId, userId) {
  try {
    const res = await fetchWithFallback(`/issues/issue?bookId=${bookId}&userId=${userId}`, {
      method: "POST",
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to issue book", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error issuing book:", error);
    return { error: "Network error while issuing book", details: error.message };
  }
}

export async function returnBook(issueId, finePaid = null) {
  try {
    let endpoint = `/issues/return?issueId=${issueId}`;
    if (finePaid !== null) {
      endpoint += `&finePaid=${finePaid}`;
    }
    
    const res = await fetchWithFallback(endpoint, {
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
  try {
    const res = await fetchWithFallback(`/reservations/reserve?bookId=${bookId}&userId=${userId}`, {
      method: "POST",
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to reserve book", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error reserving book:", error);
    return { error: "Network error while reserving book", details: error.message };
  }
}

export async function getUserIssues(userId) {
  try {
    const res = await fetchWithFallback(`/issues/user/${userId}`, {
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
  try {
    const res = await fetchWithFallback(`/reservations/user/${userId}`, {
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to get reservations", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error fetching user reservations:", error);
    return { error: "Network error while fetching reservations", details: error.message };
  }
}

// Reports and analytics
export async function getMostIssuedBooks() {
  try {
    const res = await fetchWithFallback(`/issues/reports/mostIssued`, {
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
  try {
    const res = await fetchWithFallback(`/issues/reports/userActivity/${userId}`, {
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to get user activity", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error fetching user activity:", error);
    return { error: "Network error while fetching user activity", details: error.message };
  }
}

export async function getFinesReport() {
  try {
    const res = await fetchWithFallback(`/issues/reports/fines`, {
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to get fines report", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error fetching fines report:", error);
    return { error: "Network error while fetching fines report", details: error.message };
  }
}

// User management for admins
export async function getAllUsers() {
  try {
    const res = await fetchWithFallback(`/users`, {
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to get users", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error fetching users:", error);
    return { error: "Network error while fetching users", details: error.message };
  }
}

export async function updateUserRole(userId, role) {
  try {
    const res = await fetchWithFallback(`/users/${userId}/role`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        ...authHeaders()
      },
      body: JSON.stringify({ role }),
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to update user role", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error updating user role:", error);
    return { error: "Network error while updating user role", details: error.message };
  }
}

export async function getUsersByRole(role) {
  try {
    const res = await fetchWithFallback(`/users/byRole/${role}`, {
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to get users by role", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error fetching users by role:", error);
    return { error: "Network error while fetching users by role", details: error.message };
  }
}

export async function deleteUser(userId) {
  try {
    const res = await fetchWithFallback(`/users/${userId}`, {
      method: "DELETE",
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to delete user", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error deleting user:", error);
    return { error: "Network error while deleting user", details: error.message };
  }
}

// Issue management
export async function getAllIssues() {
  try {
    const res = await fetchWithFallback(`/issues`, {
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to get issues", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error fetching issues:", error);
    return { error: "Network error while fetching issues", details: error.message };
  }
}

export async function calculateFine(issueId) {
  try {
    const res = await fetchWithFallback(`/issues/fine/${issueId}`, {
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
  try {
    const res = await fetchWithFallback(`/reservations`, {
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to get reservations", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error fetching reservations:", error);
    return { error: "Network error while fetching reservations", details: error.message };
  }
}

export async function cancelReservation(reservationId) {
  try {
    const res = await fetchWithFallback(`/reservations/${reservationId}`, {
      method: "DELETE",
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to cancel reservation", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error cancelling reservation:", error);
    return { error: "Network error while cancelling reservation", details: error.message };
  }
}

// Reservation notifications
export async function notifyReservation(reservationId) {
  try {
    const res = await fetchWithFallback(`/reservations/notify-available/${reservationId}`, {
      method: "POST",
      headers: authHeaders()
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ error: "Unauthorized" }));
      return { error: errorData.error || "Failed to send notification", status: res.status };
    }
    return res.json();
  } catch (error) {
    console.error("Error notifying reservation:", error);
    return { error: "Network error while sending notification", details: error.message };
  }
}