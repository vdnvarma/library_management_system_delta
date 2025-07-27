import React, { useState, useEffect } from "react";
import Login from "./components/Login";
import Register from "./components/Register";
import Dashboard from "./components/Dashboard";
import Navigation from "./components/Navigation";
import UserManagement from "./components/UserManagement";
import UserReports from "./components/UserReports";
import IssueManagement from "./components/IssueManagement";
import ReservationManagement from "./components/ReservationManagement";
import { getJwt } from "./api/api"; // Import the JWT helper

function App() {
  const [user, setUser] = useState(null);
  const [showRegister, setShowRegister] = useState(false);
  const [currentPage, setCurrentPage] = useState("dashboard");
  const [isLoading, setIsLoading] = useState(true);
  
  // Check for saved user session on app load
  useEffect(() => {
    const token = getJwt();
    console.log("App started, checking for saved session");
    if (token) {
      console.log("Found saved JWT token");
      // User has a token saved, try to get user info from localStorage
      const savedUser = localStorage.getItem('user');
      if (savedUser) {
        try {
          const userData = JSON.parse(savedUser);
          console.log("Restoring user session for: " + userData.username);
          setUser(userData);
        } catch (error) {
          console.error('Failed to parse saved user', error);
          localStorage.removeItem('user');
          localStorage.removeItem('jwt');
        }
      } else {
        console.log("Found token but no saved user data, clearing token");
        localStorage.removeItem('jwt');
      }
    } else {
      console.log("No saved JWT token found");
    }
    setIsLoading(false);
  }, []);

  // Render the active component based on the selected page
  const renderActiveComponent = () => {
    switch (currentPage) {
      case "dashboard":
        return <Dashboard user={user} onLogout={handleLogout} />;
      case "userManagement":
        return <UserManagement user={user} />;
      case "reports":
        return <UserReports user={user} />;
      case "issueManagement":
        return <IssueManagement user={user} />;
      case "reservationManagement":
        return <ReservationManagement user={user} />;
      case "books":
        return <Dashboard user={user} onLogout={handleLogout} initialTab="all" />;
      case "myBooks":
        return <Dashboard user={user} onLogout={handleLogout} initialTab="myBooks" />;
      default:
        return <Dashboard user={user} onLogout={handleLogout} />;
    }
  };

  const handleLogin = (userData) => {
    setUser(userData);
    // Save user data to localStorage for session persistence
    localStorage.setItem('user', JSON.stringify(userData));
  };

  const handleLogout = () => {
    setUser(null);
    setCurrentPage("dashboard");
    // Clear localStorage on logout
    localStorage.removeItem('user');
    localStorage.removeItem('jwt');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {isLoading ? (
        // Loading screen
        <div className="flex flex-col items-center justify-center min-h-screen">
          <div className="animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-blue-500"></div>
          <p className="mt-4 text-gray-600">Loading Library Management System...</p>
        </div>
      ) : !user ? (
        // Login/Register screen
        <div className="flex flex-col items-center justify-center min-h-screen p-4">
          <div className="w-full max-w-md">
            <div className="text-center mb-8">
              <h1 className="text-3xl font-bold text-blue-600 mb-2">Library Management System</h1>
              <p className="text-gray-600">Access your digital library</p>
            </div>
            
            <div className="bg-white rounded-xl shadow-lg p-6 md:p-8">
              {showRegister
                ? <Register onRegister={handleLogin} />
                : <Login onLogin={handleLogin} />}
              
              <div className="mt-6 text-center">
                <button
                  onClick={()=>setShowRegister(!showRegister)}
                  className="text-blue-600 hover:text-blue-800 underline transition duration-200"
                >
                  {showRegister ? "Already have an account? Login" : "Need an account? Register"}
                </button>
              </div>
            </div>
          </div>
        </div>
      ) : (
        // Main application
        <div>
          <header className="bg-white shadow">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
              <div className="flex items-center justify-between h-16">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <span className="text-blue-600 font-bold text-xl">LMS</span>
                  </div>
                </div>
                <div className="flex items-center">
                  <span className="text-gray-700 mr-4">
                    {user.name} ({user.role})
                  </span>
                  <button
                    onClick={handleLogout}
                    className="bg-gray-200 hover:bg-gray-300 text-gray-700 px-3 py-1 rounded-md text-sm font-medium transition duration-200"
                  >
                    Logout
                  </button>
                </div>
              </div>
            </div>
          </header>
          
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
            <Navigation 
              user={user}
              currentPage={currentPage}
              onNavigate={setCurrentPage}
            />
            <div className="mt-6">
              {renderActiveComponent()}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default App; 