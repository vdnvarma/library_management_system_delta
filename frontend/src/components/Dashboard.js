import React, { useState, useEffect } from "react";
import { 
  getBooks, 
  addBook, 
  deleteBook, 
  issueBook, 
  reserveBook, 
  getUserIssues, 
  getUserReservations, 
  getMostIssuedBooks 
} from "../api/api";
import BookList from "./BookList";
import BookSearch from "./BookSearch";

export default function Dashboard({ user, onLogout, initialTab = 'all' }) {
  const [books, setBooks] = useState([]);
  const [showAdd, setShowAdd] = useState(false);
  const [form, setForm] = useState({title:"",author:"",isbn:"",genre:"",edition:"",publisher:""});
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState(initialTab); // 'all', 'popular', 'new', 'myBooks'
  const [popularBooks, setPopularBooks] = useState([]);
  const [stats, setStats] = useState({
    currentIssues: 0,
    activeReservations: 0,
    booksOverdue: 0,
    totalBooks: 0,
    availableBooks: 0
  });

  useEffect(() => {
    setLoading(true);
    
    const fetchAllData = async () => {
      try {
        // Load books
        const booksResult = await getBooks();
        // Get user issues
        const issues = await getUserIssues(user.id);
        
        if (Array.isArray(booksResult) && Array.isArray(issues)) {
          // Mark books that are borrowed by the current user - for UI and librarian functions
          const borrowedBooks = new Set();
          const issueIdMap = {};
          
          issues.forEach(issue => {
            if (!issue.returnDate) { // If book is still borrowed (not returned)
              borrowedBooks.add(issue.book.id);
              issueIdMap[issue.book.id] = issue.id; // Map book ID to issue ID
            }
          });
          
          // Add borrowedByCurrentUser flag and issueId to each book
          const enhancedBooks = booksResult.map(book => ({
            ...book,
            borrowedByCurrentUser: borrowedBooks.has(book.id),
            issueId: issueIdMap[book.id] || null
          }));
          
          setBooks(enhancedBooks);
          setError("");
          
          // Calculate book stats
          const totalBooks = booksResult.length;
          const availableBooks = booksResult.filter(book => book.available).length;
          
          let currentIssues = 0;
          let booksOverdue = 0;
          
          if (Array.isArray(issues)) {
            currentIssues = issues.filter(issue => !issue.returnDate).length;
            booksOverdue = issues.filter(issue => {
              if (issue.returnDate) return false;
              const dueDate = new Date(issue.dueDate);
              const today = new Date();
              return today > dueDate;
            }).length;
          }
          
          // Get reservations
          const reservations = await getUserReservations(user.id);
          let activeReservations = 0;
          
          if (Array.isArray(reservations)) {
            activeReservations = reservations.filter(res => res.active).length;
          }
          
          // Get popular books
          try {
            const popularBooksResult = await getMostIssuedBooks();
            // The getMostIssuedBooks function always returns an array (empty if there was an error)
            setPopularBooks(popularBooksResult);
          } catch (err) {
            console.log("Unable to load popular books, will show empty state");
            setPopularBooks([]);
          }
          
          // Update all stats at once
          setStats({
            currentIssues,
            activeReservations,
            booksOverdue,
            totalBooks,
            availableBooks
          });
        } else {
          setBooks([]);
          setError(booksResult.error || "Failed to load books");
        }
      } catch (err) {
        console.error("Failed to load dashboard data", err);
        setError("Failed to load dashboard data. Please try again later.");
      } finally {
        setLoading(false);
      }
    };
    
    fetchAllData();
  }, [user.id]);

  const handleSearchResults = results => setBooks(results);

  const handleAddBook = async e => {
    e.preventDefault();
    await addBook(form);
    setForm({title:"",author:"",isbn:"",genre:"",edition:"",publisher:""});
    setShowAdd(false);
    getBooks().then(result => {
      if (Array.isArray(result)) {
        setBooks(result);
        setError("");
      } else {
        setBooks([]);
        setError(result.error || "Failed to load books");
      }
    });
  };

  const handleDelete = async id => {
    await deleteBook(id);
    getBooks().then(result => {
      if (Array.isArray(result)) {
        setBooks(result);
        setError("");
      } else {
        setBooks([]);
        setError(result.error || "Failed to load books");
      }
    });
  };

  const handleBorrow = async (bookId) => {
    const result = await issueBook(bookId, user.id);
    if (result && !result.error) {
      alert("Book borrowed successfully!");
      getBooks().then(result => {
        if (Array.isArray(result)) {
          setBooks(result);
          setError("");
        } else {
          setBooks([]);
          setError(result.error || "Failed to load books");
        }
      });
    } else {
      alert(result.error || "Failed to borrow book");
    }
  };

  const handleReserve = async (bookId) => {
    const result = await reserveBook(bookId, user.id);
    if (result && !result.error) {
      alert("Book reserved successfully!");
      getBooks().then(result => {
        if (Array.isArray(result)) {
          setBooks(result);
          setError("");
        } else {
          setBooks([]);
          setError(result.error || "Failed to load books");
        }
      });
    } else {
      alert(result.error || "Failed to reserve book");
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      <div className="bg-white rounded-xl shadow-lg p-6 mb-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <h2 className="text-2xl font-bold text-gray-800">
            Welcome, {user.name} 
            <span className="text-lg font-normal text-gray-500 ml-2">({user.role})</span>
          </h2>
          <button 
            onClick={onLogout} 
            className="bg-gray-600 text-white py-2 px-4 rounded-lg font-medium hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition duration-200"
          >
            Logout
          </button>
        </div>
      </div>

      {/* Library Stats Overview */}
      <div className="bg-white rounded-xl shadow-lg p-6 mb-6">
        <h3 className="text-lg font-semibold text-gray-800 mb-4">Library Statistics</h3>
        <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
          <div className="bg-blue-50 rounded-lg p-4 flex flex-col items-center justify-center text-center">
            <div className="text-blue-500 text-2xl font-bold">{stats.totalBooks}</div>
            <div className="text-blue-700 mt-1 text-sm font-medium">Total Books</div>
          </div>
          <div className="bg-green-50 rounded-lg p-4 flex flex-col items-center justify-center text-center">
            <div className="text-green-500 text-2xl font-bold">{stats.availableBooks}</div>
            <div className="text-green-700 mt-1 text-sm font-medium">Available Books</div>
          </div>
          <div className="bg-indigo-50 rounded-lg p-4 flex flex-col items-center justify-center text-center">
            <div className="text-indigo-500 text-2xl font-bold">{stats.currentIssues}</div>
            <div className="text-indigo-700 mt-1 text-sm font-medium">Borrowed by You</div>
          </div>
          <div className="bg-purple-50 rounded-lg p-4 flex flex-col items-center justify-center text-center">
            <div className="text-purple-500 text-2xl font-bold">{stats.activeReservations}</div>
            <div className="text-purple-700 mt-1 text-sm font-medium">Your Reservations</div>
          </div>
          <div className={`${stats.booksOverdue > 0 ? 'bg-red-50' : 'bg-green-50'} rounded-lg p-4 flex flex-col items-center justify-center text-center`}>
            <div className={`${stats.booksOverdue > 0 ? 'text-red-500' : 'text-green-500'} text-2xl font-bold`}>
              {stats.booksOverdue}
            </div>
            <div className={`${stats.booksOverdue > 0 ? 'text-red-700' : 'text-green-700'} mt-1 text-sm font-medium`}>
              {stats.booksOverdue === 1 ? 'Book Overdue' : 'Books Overdue'}
            </div>
          </div>
        </div>
      </div>
      
      {/* Book Collection Tabs */}
      <div className="bg-white rounded-xl shadow-lg p-6 mb-6">
        <div className="flex border-b border-gray-200 mb-6 overflow-x-auto">
          <button
            className={`px-4 py-2 border-b-2 font-medium text-sm ${
              activeTab === 'all' 
                ? 'border-blue-500 text-blue-600' 
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
            onClick={() => setActiveTab('all')}
          >
            All Books
          </button>
          <button
            className={`px-4 py-2 border-b-2 font-medium text-sm ${
              activeTab === 'popular' 
                ? 'border-blue-500 text-blue-600' 
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
            onClick={() => setActiveTab('popular')}
          >
            Popular Books
          </button>
          <button
            className={`px-4 py-2 border-b-2 font-medium text-sm ${
              activeTab === 'new' 
                ? 'border-blue-500 text-blue-600' 
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
            onClick={() => setActiveTab('new')}
          >
            New Arrivals
          </button>
        </div>

        {loading ? (
          <div className="flex justify-center items-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
          </div>
        ) : (
          <div>
            {activeTab === 'all' && (
              <div>
                <h3 className="text-lg font-semibold text-gray-800 mb-4">All Library Books</h3>
                <p className="text-gray-600 mb-4">Browse our complete collection of books available in the library.</p>
              </div>
            )}
            
            {activeTab === 'popular' && (
              <div>
                <h3 className="text-lg font-semibold text-gray-800 mb-4">Most Popular Books</h3>
                <p className="text-gray-600 mb-4">These are the most frequently borrowed books in our library.</p>
                {!popularBooks || popularBooks.length === 0 ? (
                  <p className="text-gray-500 text-center py-4">No borrowing history available yet.</p>
                ) : (
                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
                    {popularBooks.slice(0, 6).map((book, index) => (
                      <div key={book.id || index} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition duration-200">
                        <div className="flex items-center gap-2 mb-2">
                          <span className="bg-blue-100 text-blue-800 text-xs font-medium px-2 py-0.5 rounded">
                            #{index + 1} Popular
                          </span>
                        </div>
                        <h4 className="font-semibold text-blue-600">{book.title}</h4>
                        <p className="text-gray-600 text-sm">by {book.author}</p>
                        <p className="text-gray-500 text-xs mt-2">Borrowed {book.issueCount || '0'} times</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
            
            {activeTab === 'new' && (
              <div>
                <h3 className="text-lg font-semibold text-gray-800 mb-4">New Arrivals</h3>
                <p className="text-gray-600 mb-4">Check out the latest books added to our collection.</p>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
                  {books.slice(0, 6).map(book => (
                    <div key={book.id} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition duration-200">
                      <div className="flex items-center gap-2 mb-2">
                        <span className="bg-green-100 text-green-800 text-xs font-medium px-2 py-0.5 rounded">New</span>
                      </div>
                      <h4 className="font-semibold text-blue-600">{book.title}</h4>
                      <p className="text-gray-600 text-sm">by {book.author}</p>
                      <p className="text-gray-500 text-xs mt-2">Added recently</p>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}
      <BookSearch onResults={handleSearchResults} />
      <BookList 
        books={books} 
        onDelete={handleDelete} 
        user={user} 
        onBorrow={handleBorrow} 
        onReserve={handleReserve} 
        onRefresh={() => {
          setLoading(true);
          Promise.all([getBooks(), getUserIssues(user.id)])
            .then(([booksResult, issues]) => {
              if (Array.isArray(booksResult) && Array.isArray(issues)) {
                const borrowedBooks = new Set();
                const issueIdMap = {};
                
                issues.forEach(issue => {
                  if (!issue.returnDate) { // If book is still borrowed
                    borrowedBooks.add(issue.book.id);
                    issueIdMap[issue.book.id] = issue.id;
                  }
                });
                
                const enhancedBooks = booksResult.map(book => ({
                  ...book,
                  borrowedByCurrentUser: borrowedBooks.has(book.id),
                  issueId: issueIdMap[book.id] || null
                }));
                
                setBooks(enhancedBooks);
                setError("");
              }
              setLoading(false);
            })
            .catch(err => {
              console.error("Failed to refresh books", err);
              setError("Failed to refresh book list");
              setLoading(false);
            });
        }} 
      />
      {user.role !== "STUDENT" && (
        <div className="bg-white rounded-xl shadow-lg p-6 mt-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">Library Management</h3>
          
          <button 
            onClick={()=>setShowAdd(!showAdd)} 
            className={`flex items-center ${showAdd ? 'bg-gray-500' : 'bg-green-600'} text-white py-3 px-6 rounded-lg font-medium hover:opacity-90 focus:outline-none focus:ring-2 focus:ring-offset-2 transition duration-200`}
            style={{transition: 'all 0.3s ease'}}
          >
            {showAdd ? (
              <>
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
                Cancel
              </>
            ) : (
              <>
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
                Add New Book
              </>
            )}
          </button>
          
          {showAdd && (
            <div className="mt-6 bg-gray-50 rounded-xl p-6 border border-gray-200">
              <h4 className="font-medium text-gray-700 mb-4">Enter Book Details</h4>
              <form onSubmit={handleAddBook} className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">Title</label>
                  <input 
                    id="title"
                    placeholder="Enter book title" 
                    value={form.title} 
                    onChange={e=>setForm({...form, title:e.target.value})} 
                    required 
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200" 
                  />
                </div>
                
                <div>
                  <label htmlFor="author" className="block text-sm font-medium text-gray-700 mb-1">Author</label>
                  <input 
                    id="author"
                    placeholder="Enter author name" 
                    value={form.author} 
                    onChange={e=>setForm({...form, author:e.target.value})} 
                    required 
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200" 
                  />
                </div>
                
                <div>
                  <label htmlFor="isbn" className="block text-sm font-medium text-gray-700 mb-1">ISBN</label>
                  <input 
                    id="isbn"
                    placeholder="Enter ISBN number" 
                    value={form.isbn} 
                    onChange={e=>setForm({...form, isbn:e.target.value})} 
                    required 
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200" 
                  />
                </div>
                
                <div>
                  <label htmlFor="genre" className="block text-sm font-medium text-gray-700 mb-1">Genre</label>
                  <select
                    id="genre"
                    value={form.genre}
                    onChange={e=>setForm({...form, genre:e.target.value})}
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
                  >
                    <option value="">Select a genre</option>
                    <option value="Fiction">Fiction</option>
                    <option value="Non-Fiction">Non-Fiction</option>
                    <option value="Science Fiction">Science Fiction</option>
                    <option value="Fantasy">Fantasy</option>
                    <option value="Mystery">Mystery</option>
                    <option value="Biography">Biography</option>
                    <option value="History">History</option>
                    <option value="Science">Science</option>
                    <option value="Technology">Technology</option>
                    <option value="Self-Help">Self-Help</option>
                    <option value="Other">Other</option>
                  </select>
                </div>
                
                <div>
                  <label htmlFor="edition" className="block text-sm font-medium text-gray-700 mb-1">Edition</label>
                  <input 
                    id="edition"
                    placeholder="Edition (optional)" 
                    value={form.edition} 
                    onChange={e=>setForm({...form, edition:e.target.value})} 
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200" 
                  />
                </div>
                
                <div>
                  <label htmlFor="publisher" className="block text-sm font-medium text-gray-700 mb-1">Publisher</label>
                  <input 
                    id="publisher"
                    placeholder="Publisher (optional)" 
                    value={form.publisher} 
                    onChange={e=>setForm({...form, publisher:e.target.value})} 
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200" 
                  />
                </div>
                
                <div className="md:col-span-2 mt-4 flex justify-end gap-3">
                  <button 
                    type="button"
                    onClick={() => setShowAdd(false)}
                    className="px-6 py-3 border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition duration-200"
                  >
                    Cancel
                  </button>
                  <button 
                    type="submit" 
                    className="bg-blue-600 text-white py-3 px-6 rounded-lg font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition duration-200 flex items-center"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                    </svg>
                    Add Book to Library
                  </button>
                </div>
              </form>
            </div>
          )}
        </div>
      )}
    </div>
  );
} 