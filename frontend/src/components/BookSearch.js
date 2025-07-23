import React, { useState, useEffect } from "react";
import { searchBooks, getBooks } from "../api/api";

export default function BookSearch({ onResults }) {
  const [type, setType] = useState("title");
  const [keyword, setKeyword] = useState("");
  const [searching, setSearching] = useState(false);
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);

  // Used for showing suggestions when typing
  useEffect(() => {
    if (keyword.length > 1) {
      const fetchSuggestions = async () => {
        const results = await searchBooks(type, keyword);
        if (Array.isArray(results)) {
          setSuggestions(results.slice(0, 5)); // Show top 5 suggestions
        }
      };
      fetchSuggestions();
    } else {
      setSuggestions([]);
    }
  }, [keyword, type]);

  const handleSearch = async e => {
    e.preventDefault();
    setSearching(true);
    setShowSuggestions(false);
    try {
      const results = await searchBooks(type, keyword);
      onResults(results);
    } finally {
      setSearching(false);
    }
  };

  const handleReset = async () => {
    setKeyword("");
    setSuggestions([]);
    setSearching(true);
    try {
      const results = await getBooks();
      onResults(results);
    } finally {
      setSearching(false);
    }
  };

  const handleSuggestionClick = async (suggestion) => {
    setKeyword(suggestion.title);
    setShowSuggestions(false);
    setSearching(true);
    try {
      const results = await searchBooks("title", suggestion.title);
      onResults(results);
    } finally {
      setSearching(false);
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-lg p-6 mb-6">
      <h3 className="text-xl font-semibold text-gray-800 mb-4">Search Books</h3>
      <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-4">
        <select 
          value={type} 
          onChange={e=>setType(e.target.value)} 
          className="px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200 bg-white"
        >
          <option value="title">Search by Title</option>
          <option value="author">Search by Author</option>
          <option value="isbn">Search by ISBN</option>
          <option value="genre">Search by Genre</option>
        </select>
        <div className="flex-1 relative">
          <input 
            value={keyword} 
            onChange={e=>setKeyword(e.target.value)} 
            onFocus={() => setShowSuggestions(true)}
            onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
            placeholder="Enter search term..." 
            required 
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200" 
          />
          {showSuggestions && suggestions.length > 0 && (
            <div className="absolute z-10 w-full mt-1 bg-white rounded-md shadow-lg border border-gray-200">
              <ul className="py-1 overflow-auto text-base">
                {suggestions.map(suggestion => (
                  <li 
                    key={suggestion.id}
                    onClick={() => handleSuggestionClick(suggestion)}
                    className="cursor-pointer px-4 py-2 hover:bg-gray-100 text-gray-700"
                  >
                    {suggestion.title} - <span className="text-gray-500 text-sm">{suggestion.author}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
        <div className="flex gap-2">
          <button 
            type="submit" 
            disabled={searching}
            className={`bg-blue-600 text-white py-3 px-6 rounded-lg font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition duration-200 whitespace-nowrap ${
              searching ? 'opacity-70 cursor-not-allowed' : ''
            }`}
          >
            {searching ? (
              <div className="flex items-center">
                <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Searching...
              </div>
            ) : 'Search Library'}
          </button>
          {keyword && (
            <button
              type="button"
              onClick={handleReset}
              className="bg-gray-200 text-gray-700 py-3 px-4 rounded-lg font-medium hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition duration-200"
            >
              Reset
            </button>
          )}
        </div>
      </form>
    </div>
  );
} 