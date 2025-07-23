import React, { useState, useEffect } from "react";
import { getUserIssues, getAllIssues, returnBook, calculateFine } from "../api/api";

export default function IssueManagement({ user }) {
  const [issues, setIssues] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [viewMode, setViewMode] = useState(user.role === "STUDENT" ? "my" : "all");
  const [selectedIssue, setSelectedIssue] = useState(null);
  const [fineDetails, setFineDetails] = useState(null);
  const [finePaid, setFinePaid] = useState("");

  useEffect(() => {
    const fetchIssues = async () => {
      setLoading(true);
      setError("");
      
      try {
        let result;
        if (viewMode === "my" || user.role === "STUDENT") {
          result = await getUserIssues(user.id);
        } else {
          result = await getAllIssues();
        }
        
        if (result.error) {
          setError(result.error);
          setIssues([]);
        } else {
          setIssues(Array.isArray(result) ? result : []);
        }
      } catch (err) {
        setError("Failed to load issue records");
        setIssues([]);
      } finally {
        setLoading(false);
      }
    };
    
    fetchIssues();
  }, [user.id, user.role, viewMode]);

  const handleReturnBook = async () => {
    if (!selectedIssue) return;
    
    try {
      const result = await returnBook(selectedIssue.id, finePaid ? parseFloat(finePaid) : null);
      if (!result.error) {
        // Update the issues list with the returned book
        setIssues(issues.map(issue => 
          issue.id === selectedIssue.id ? result : issue
        ));
        setSelectedIssue(null);
        setFineDetails(null);
        setFinePaid("");
        setError("");
      } else {
        setError(result.error);
      }
    } catch (err) {
      setError("Failed to return book");
    }
  };

  const handleShowFineDetails = async (issue) => {
    setSelectedIssue(issue);
    setLoading(true);
    setError("");
    
    try {
      const result = await calculateFine(issue.id);
      if (result.error) {
        setError(result.error);
        setFineDetails(null);
      } else {
        setFineDetails(result);
        setFinePaid(result.fineAmount.toString());
      }
    } catch (err) {
      setError("Failed to calculate fine");
      setFineDetails(null);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    return new Date(dateString).toLocaleDateString();
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount);
  };

  const isOverdue = (issue) => {
    if (issue.returnDate) return false;
    const dueDate = new Date(issue.dueDate);
    const today = new Date();
    return today > dueDate;
  };

  return (
    <div className="bg-white rounded-xl shadow-lg p-6">
      {selectedIssue && fineDetails ? (
        <div>
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-bold text-gray-800">Return Book</h2>
            <button
              onClick={() => {
                setSelectedIssue(null);
                setFineDetails(null);
              }}
              className="text-gray-500 hover:text-gray-700"
            >
              &times;
            </button>
          </div>
          
          <div className="bg-gray-50 rounded-lg p-6 mb-6">
            <h3 className="text-lg font-semibold text-gray-700 mb-4">{fineDetails.bookTitle}</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-gray-500">Borrowed by: {fineDetails.userName}</p>
                <p className="text-sm text-gray-500">Due date: {formatDate(fineDetails.dueDate)}</p>
              </div>
              <div>
                {fineDetails.daysOverdue > 0 ? (
                  <div>
                    <p className="text-red-600 font-semibold">
                      {fineDetails.daysOverdue} days overdue
                    </p>
                    <p className="text-lg font-bold mt-2">
                      Fine: {formatCurrency(fineDetails.fineAmount)}
                    </p>
                    <div className="mt-4">
                      <label htmlFor="finePaid" className="block text-sm font-medium text-gray-700 mb-1">
                        Fine Paid Amount:
                      </label>
                      <input
                        type="number"
                        id="finePaid"
                        value={finePaid}
                        onChange={(e) => setFinePaid(e.target.value)}
                        min="0"
                        step="0.01"
                        className="border border-gray-300 rounded px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                  </div>
                ) : (
                  <p className="text-green-600 font-semibold">On time - No fine</p>
                )}
              </div>
            </div>
            <div className="mt-6">
              <button
                onClick={handleReturnBook}
                className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition duration-200"
              >
                Confirm Return
              </button>
            </div>
          </div>
        </div>
      ) : (
        <div>
          <div className="flex flex-col md:flex-row items-start md:items-center justify-between mb-6">
            <h2 className="text-xl font-bold text-gray-800 mb-4 md:mb-0">Book Issues & Returns</h2>
            
            {user.role !== "STUDENT" && (
              <div className="inline-flex rounded-md shadow-sm" role="group">
                <button
                  type="button"
                  onClick={() => setViewMode("all")}
                  className={`px-4 py-2 text-sm font-medium rounded-l-md focus:z-10 focus:outline-none ${
                    viewMode === "all"
                      ? "bg-blue-600 text-white hover:bg-blue-700"
                      : "bg-white text-gray-700 hover:bg-gray-50 border border-gray-300"
                  }`}
                >
                  All Issues
                </button>
                <button
                  type="button"
                  onClick={() => setViewMode("my")}
                  className={`px-4 py-2 text-sm font-medium rounded-r-md focus:z-10 focus:outline-none ${
                    viewMode === "my"
                      ? "bg-blue-600 text-white hover:bg-blue-700"
                      : "bg-white text-gray-700 hover:bg-gray-50 border border-gray-300"
                  }`}
                >
                  My Issues
                </button>
              </div>
            )}
          </div>
          
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              {error}
            </div>
          )}
          
          {loading ? (
            <div className="flex items-center justify-center py-10">
              <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-blue-500"></div>
            </div>
          ) : (
            <div className="overflow-x-auto">
              {issues.length === 0 ? (
                <div className="text-center py-8">
                  <div className="text-gray-400 text-lg mb-2">📚</div>
                  <p className="text-gray-500">No issue records found.</p>
                </div>
              ) : (
                <table className="min-w-full bg-white">
                  <thead className="bg-gray-50 border-b">
                    <tr>
                      <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Book Title</th>
                      <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Borrowed By</th>
                      <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Issue Date</th>
                      <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Due Date</th>
                      <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Return Date</th>
                      <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Status</th>
                      <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200">
                    {issues.map(issue => (
                      <tr key={issue.id} className={`hover:bg-gray-50 transition duration-150 ${isOverdue(issue) ? 'bg-red-50' : ''}`}>
                        <td className="py-3 px-4 text-sm">{issue.book.title}</td>
                        <td className="py-3 px-4 text-sm">{issue.user.name}</td>
                        <td className="py-3 px-4 text-sm">{formatDate(issue.issueDate)}</td>
                        <td className="py-3 px-4 text-sm">{formatDate(issue.dueDate)}</td>
                        <td className="py-3 px-4 text-sm">{formatDate(issue.returnDate)}</td>
                        <td className="py-3 px-4">
                          {issue.returnDate ? (
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                              Returned
                            </span>
                          ) : isOverdue(issue) ? (
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
                              Overdue
                            </span>
                          ) : (
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                              Borrowed
                            </span>
                          )}
                        </td>
                        <td className="py-3 px-4">
                          {!issue.returnDate && (
                            <button
                              onClick={() => handleShowFineDetails(issue)}
                              className="bg-blue-600 hover:bg-blue-700 text-white text-xs py-1 px-2 rounded transition duration-200"
                            >
                              Return
                            </button>
                          )}
                          {issue.finePaid > 0 && (
                            <div className="mt-1 text-xs text-gray-500">
                              Fine paid: {formatCurrency(issue.finePaid)}
                            </div>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
