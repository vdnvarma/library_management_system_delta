import React, { useState } from "react";

export default function Navigation({ currentPage, onNavigate, user }) {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  
  const tabs = [
    { id: "dashboard", label: "Dashboard", icon: "M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6", allowedRoles: ["ADMIN", "LIBRARIAN", "STUDENT"] },
    { id: "books", label: "Books", icon: "M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253", allowedRoles: ["ADMIN", "LIBRARIAN", "STUDENT"] },
    { id: "myBooks", label: "My Books", icon: "M16 4v12l-4-2-4 2V4M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z", allowedRoles: ["STUDENT"] },
    { id: "issueManagement", label: "Issues & Returns", icon: "M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2", allowedRoles: ["ADMIN", "LIBRARIAN"] },
    { id: "reservationManagement", label: "Reservations", icon: "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z", allowedRoles: ["ADMIN", "LIBRARIAN"] },
    { id: "reports", label: "Reports", icon: "M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z", allowedRoles: ["ADMIN", "LIBRARIAN"] },
    { id: "userManagement", label: "Users", icon: "M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z", allowedRoles: ["ADMIN"] }
  ];

  // Filter tabs based on user role
  const allowedTabs = tabs.filter(tab => tab.allowedRoles.includes(user.role));
  
  return (
    <nav className="bg-white shadow-lg rounded-xl mb-6">
      {/* Desktop Navigation */}
      <div className="hidden md:block">
        <ul className="flex">
          {allowedTabs.map(tab => (
            <li key={tab.id}>
              <button
                onClick={() => onNavigate(tab.id)}
                className={`flex items-center px-4 py-3 text-gray-700 hover:bg-gray-100 hover:text-blue-600 ${
                  currentPage === tab.id ? 'text-blue-600 border-b-2 border-blue-600' : ''
                }`}
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d={tab.icon} />
                </svg>
                {tab.label}
              </button>
            </li>
          ))}
        </ul>
      </div>

      {/* Mobile Navigation */}
      <div className="md:hidden">
        <div className="flex items-center justify-between px-4 py-3">
          <span className="text-lg font-semibold text-blue-600">
            {allowedTabs.find(tab => tab.id === currentPage)?.label || 'Library App'}
          </span>
          <button 
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            className="p-2 rounded-md text-gray-700 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
        </div>
        
        {isMobileMenuOpen && (
          <div className="px-2 pb-3 space-y-1">
            {allowedTabs.map(tab => (
              <button
                key={tab.id}
                onClick={() => {
                  onNavigate(tab.id);
                  setIsMobileMenuOpen(false);
                }}
                className={`flex items-center w-full px-3 py-2 rounded-md text-sm font-medium ${
                  currentPage === tab.id
                    ? 'bg-blue-100 text-blue-700'
                    : 'text-gray-700 hover:bg-gray-100'
                }`}
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d={tab.icon} />
                </svg>
                {tab.label}
              </button>
            ))}
          </div>
        )}
      </div>
    </nav>
  );
}
