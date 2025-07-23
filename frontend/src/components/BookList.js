import React, { useState, useEffect } from "react";
import { calculateFine, returnBook } from "../api/api";

export default function BookList({ books, onDelete, user, onBorrow, onReserve, onRefresh }) {
  const [expandedBookId, setExpandedBookId] = useState(null);
  const [viewMode, setViewMode] = useState(() => {
    // Get saved preference from localStorage or default to 'grid'
    return localStorage.getItem('bookViewMode') || 'grid';
  });
  const [filteredBooks, setFilteredBooks] = useState(books);
  const [filterOption, setFilterOption] = useState('all');
  const [sortOption, setSortOption] = useState('title');
  const [isLoading, setIsLoading] = useState(false);
  
  useEffect(() => {
    // Save preference to localStorage when viewMode changes
    localStorage.setItem('bookViewMode', viewMode);
  }, [viewMode]);
  
  useEffect(() => {
    setIsLoading(true);
    // Apply filters and sorting
    let result = [...books];
    
    // Filter
    if (filterOption === 'available') {
      result = result.filter(book => book.available);
    } else if (filterOption === 'unavailable') {
      result = result.filter(book => !book.available);
    }
    
    // Sort
    result.sort((a, b) => {
      switch(sortOption) {
        case 'title':
          return a.title.localeCompare(b.title);
        case 'author':
          return a.author.localeCompare(b.author);
        case 'newest':
          return new Date(b.createdAt || 0) - new Date(a.createdAt || 0);
        default:
          return 0;
      }
    });
    
    setFilteredBooks(result);
    setIsLoading(false);
  }, [books, filterOption, sortOption]);
  
  // Utility functions for notifications
  const showNotification = (message) => {
    alert(message); // Simple implementation, could be replaced with a better UI component
  };

  const showError = (message) => {
    console.error(message);
    alert("Error: " + message);
  };

  // Function to refresh books list
  const fetchUserBooks = () => {
    if (onRefresh) {
      onRefresh();
    }
  };

  const toggleExpand = (bookId) => {
    if (expandedBookId === bookId) {
      setExpandedBookId(null);
    } else {
      setExpandedBookId(bookId);
    }
  };

  const handleReturn = async (id) => {
    try {
      // Check for user role - only non-students can return books
      if (user && user.role === "STUDENT") {
        showError("Students cannot return books. Please contact a librarian.");
        return;
      }

      if (!id) {
        showError("Cannot return this book: missing ID reference");
        return;
      }
      
      // Calculate fine first
      try {
        const fineResponse = await calculateFine(id);
        const fine = fineResponse.fineAmount;
        
        // Confirm with the user
        if (fine > 0) {
          // eslint-disable-next-line no-restricted-globals
          if (!confirm(`This book is overdue. A fine of ${fine} is applicable. Proceed with return?`)) {
            return;
          }
        }
        
        // Process the return
        await returnBook(id, fine);
        // Refresh the book list
        fetchUserBooks();
        showNotification("Book returned successfully");
      } catch (apiError) {
        // Handle specific API errors
        if (apiError.message && apiError.message.includes("403")) {
          showError("Permission denied. Only librarians and admins can return books.");
        } else {
          throw apiError; // Re-throw for the outer catch
        }
      }
    } catch (error) {
      showError("Failed to return book: " + (error.message || "Unknown error"));
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-lg p-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
        <h3 className="text-xl font-semibold text-gray-800">Library Books ({filteredBooks.length})</h3>
        
        <div className="flex flex-wrap gap-2 items-center">
          {/* Filter dropdown */}
          <div className="relative">
            <select 
              value={filterOption}
              onChange={(e) => setFilterOption(e.target.value)}
              className="appearance-none bg-white border border-gray-300 rounded-md py-2 pl-3 pr-10 text-sm leading-5 text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="all">All Books</option>
              <option value="available">Available Only</option>
              <option value="unavailable">Unavailable Only</option>
            </select>
            <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700">
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </div>
          </div>
          
          {/* Sort dropdown */}
          <div className="relative">
            <select 
              value={sortOption}
              onChange={(e) => setSortOption(e.target.value)}
              className="appearance-none bg-white border border-gray-300 rounded-md py-2 pl-3 pr-10 text-sm leading-5 text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="title">Sort by Title</option>
              <option value="author">Sort by Author</option>
              <option value="newest">Sort by Newest</option>
            </select>
            <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700">
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </div>
          </div>
          
          {/* View mode toggle */}
          <div className="inline-flex rounded-md shadow-sm" role="group">
            <button
              type="button"
              onClick={() => setViewMode('grid')}
              className={`px-4 py-2 text-sm font-medium rounded-l-md focus:z-10 focus:outline-none ${
                viewMode === 'grid'
                  ? "bg-blue-600 text-white hover:bg-blue-700"
                  : "bg-white text-gray-700 hover:bg-gray-50 border border-gray-300"
              }`}
              title="Grid View"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
              </svg>
            </button>
            <button
              type="button"
              onClick={() => setViewMode('list')}
              className={`px-4 py-2 text-sm font-medium rounded-r-md focus:z-10 focus:outline-none ${
                viewMode === 'list'
                  ? "bg-blue-600 text-white hover:bg-blue-700"
                  : "bg-white text-gray-700 hover:bg-gray-50 border border-gray-300"
              }`}
              title="List View"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      {isLoading ? (
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
        </div>
      ) : filteredBooks.length === 0 ? (
        <div className="text-center py-8">
          <div className="text-gray-400 text-lg mb-2">📚</div>
          <p className="text-gray-500">No books found. Try adjusting your search or filters.</p>
        </div>
      ) : viewMode === 'grid' ? (
        // Grid view
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredBooks.map(book => (
            <div 
              key={book.id} 
              className={`border border-gray-200 rounded-lg hover:shadow-md transition duration-200 overflow-hidden flex flex-col ${
                expandedBookId === book.id ? 'ring-2 ring-blue-500' : ''
              }`}
            >
              {/* Book cover simulation */}
              <div 
                className="h-40 bg-gradient-to-r from-blue-400 to-indigo-500 flex items-center justify-center p-4 cursor-pointer"
                onClick={() => toggleExpand(book.id)}
              >
                <h3 className="font-bold text-white text-center">{book.title}</h3>
              </div>
              
              <div className="p-4 flex-grow flex flex-col">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-gray-600 font-medium text-sm">By {book.author}</span>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${book.available ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                    {book.available ? "Available" : "Not Available"}
                  </span>
                </div>
                
                {expandedBookId === book.id && (
                  <div className="text-gray-600 text-sm space-y-1 mt-2">
                    {book.isbn && <p><span className="font-medium">ISBN:</span> {book.isbn}</p>}
                    {book.genre && <p><span className="font-medium">Genre:</span> {book.genre}</p>}
                    {book.edition && <p><span className="font-medium">Edition:</span> {book.edition}</p>}
                    {book.publisher && <p><span className="font-medium">Publisher:</span> {book.publisher}</p>}
                  </div>
                )}
                
                <div className="mt-auto pt-4 flex space-x-2">
                  {user && user.role !== "STUDENT" && (
                    <button 
                      onClick={() => onDelete(book.id)} 
                      className="bg-red-600 text-white py-1 px-2 text-xs rounded-lg font-medium hover:bg-red-700 transition duration-200"
                    >
                      Delete
                    </button>
                  )}
                  {user && user.role === "STUDENT" && book.available && (
                    <button
                      onClick={() => onBorrow(book.id)}
                      className="bg-blue-600 text-white py-1 px-2 text-xs rounded-lg font-medium hover:bg-blue-700 transition duration-200"
                    >
                      Borrow
                    </button>
                  )}
                  {user && user.role === "STUDENT" && !book.available && !book.borrowedByCurrentUser && (
                    <button
                      onClick={() => onReserve(book.id)}
                      className="bg-yellow-500 text-white py-1 px-2 text-xs rounded-lg font-medium hover:bg-yellow-600 transition duration-200"
                    >
                      Reserve
                    </button>
                  )}
                  {user && user.role !== "STUDENT" && book.borrowedByCurrentUser && (
                    <button
                      onClick={() => handleReturn(book.issueId || book.id)}
                      className="bg-green-600 text-white py-1 px-2 text-xs rounded-lg font-medium hover:bg-green-700 transition duration-200"
                    >
                      Return
                    </button>
                  )}
                  <button
                    onClick={() => toggleExpand(book.id)}
                    className="bg-gray-200 text-gray-700 py-1 px-2 text-xs rounded-lg font-medium hover:bg-gray-300 transition duration-200"
                  >
                    {expandedBookId === book.id ? 'Less Info' : 'More Info'}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        // List view
        <div className="space-y-4">
          {filteredBooks.map(book => (
            <div key={book.id} className="flex flex-col sm:flex-row sm:items-center sm:justify-between p-4 border border-gray-200 rounded-lg hover:shadow-md transition duration-200">
              <div className="flex-1">
                <div className="flex flex-wrap items-center gap-2 mb-2">
                  <span className="font-semibold text-blue-600 text-lg">{book.title}</span>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${book.available ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                    {book.available ? "Available" : "Not Available"}
                  </span>
                </div>
                <div className="text-gray-600 space-y-1">
                  <p><span className="font-medium">Author:</span> {book.author}</p>
                  {book.isbn && <p><span className="font-medium">ISBN:</span> {book.isbn}</p>}
                  {book.genre && <p><span className="font-medium">Genre:</span> {book.genre}</p>}
                  {book.edition && <p><span className="font-medium">Edition:</span> {book.edition}</p>}
                  {book.publisher && <p><span className="font-medium">Publisher:</span> {book.publisher}</p>}
                </div>
              </div>
              <div className="mt-4 sm:mt-0 sm:ml-4 flex flex-col gap-2">
                {user && user.role !== "STUDENT" && (
                  <button 
                    onClick={() => onDelete(book.id)} 
                    className="bg-red-600 text-white py-2 px-4 rounded-lg font-medium hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 transition duration-200"
                  >
                    Delete Book
                  </button>
                )}
                {user && user.role === "STUDENT" && book.available && (
                  <button
                    onClick={() => onBorrow(book.id)}
                    className="bg-blue-600 text-white py-2 px-4 rounded-lg font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition duration-200"
                  >
                    Borrow
                  </button>
                )}
                {user && user.role === "STUDENT" && !book.available && !book.borrowedByCurrentUser && (
                  <button
                    onClick={() => onReserve(book.id)}
                    className="bg-yellow-500 text-white py-2 px-4 rounded-lg font-medium hover:bg-yellow-600 focus:outline-none focus:ring-2 focus:ring-yellow-500 focus:ring-offset-2 transition duration-200"
                  >
                    Reserve
                  </button>
                )}
                {user && user.role !== "STUDENT" && book.borrowedByCurrentUser && (
                  <button
                    onClick={() => handleReturn(book.issueId || book.id)}
                    className="bg-green-600 text-white py-2 px-4 rounded-lg font-medium hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 transition duration-200"
                  >
                    Return Book
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}