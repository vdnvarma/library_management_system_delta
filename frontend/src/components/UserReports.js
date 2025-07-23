import React, { useState, useEffect } from "react";
import { getUserActivityReport, getFinesReport, getMostIssuedBooks } from "../api/api";

export default function UserReports({ user }) {
  const [activeTab, setActiveTab] = useState("activity");
  const [userReport, setUserReport] = useState(null);
  const [finesReport, setFinesReport] = useState(null);
  const [popularBooks, setPopularBooks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchReports = async () => {
      setLoading(true);
      setError("");

      try {
        switch (activeTab) {
          case "activity":
            const activityResult = await getUserActivityReport(user.id);
            if (activityResult.error) {
              setError(activityResult.error);
              setUserReport(null);
            } else {
              setUserReport(activityResult);
            }
            break;
          case "fines":
            if (user.role === "ADMIN" || user.role === "LIBRARIAN") {
              const finesResult = await getFinesReport();
              if (finesResult.error) {
                setError(finesResult.error);
                setFinesReport(null);
              } else {
                setFinesReport(finesResult);
              }
            }
            break;
          case "popular":
            const popularResult = await getMostIssuedBooks();
            if (popularResult.error) {
              setError(popularResult.error);
              setPopularBooks([]);
            } else {
              setPopularBooks(popularResult);
            }
            break;
          default:
            break;
        }
      } catch (err) {
        setError(`Failed to load ${activeTab} report`);
      } finally {
        setLoading(false);
      }
    };

    fetchReports();
  }, [activeTab, user.id, user.role]);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount);
  };

  const renderActivityReport = () => {
    if (!userReport) return null;
    
    return (
      <div className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-blue-50 p-4 rounded-lg shadow">
            <h3 className="text-lg font-medium text-blue-800">Total Books Issued</h3>
            <p className="text-3xl font-bold text-blue-600">{userReport.totalBooksIssued}</p>
          </div>
          <div className="bg-green-50 p-4 rounded-lg shadow">
            <h3 className="text-lg font-medium text-green-800">Currently Borrowed</h3>
            <p className="text-3xl font-bold text-green-600">{userReport.totalCurrentlyBorrowed}</p>
          </div>
          <div className="bg-yellow-50 p-4 rounded-lg shadow">
            <h3 className="text-lg font-medium text-yellow-800">Total Fines Paid</h3>
            <p className="text-3xl font-bold text-yellow-600">{formatCurrency(userReport.totalFinesPaid)}</p>
          </div>
        </div>

        {userReport.currentBorrows && userReport.currentBorrows.length > 0 && (
          <div>
            <h3 className="text-xl font-semibold text-gray-700 mb-3">Currently Borrowed Books</h3>
            <div className="overflow-x-auto bg-white rounded-lg shadow">
              <table className="min-w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="py-2 px-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Title</th>
                    <th className="py-2 px-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Author</th>
                    <th className="py-2 px-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Issue Date</th>
                    <th className="py-2 px-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Due Date</th>
                    <th className="py-2 px-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {userReport.currentBorrows.map((borrow) => (
                    <tr key={borrow.issueId} className="hover:bg-gray-50">
                      <td className="py-2 px-3 text-sm">{borrow.book.title}</td>
                      <td className="py-2 px-3 text-sm">{borrow.book.author}</td>
                      <td className="py-2 px-3 text-sm">{borrow.issueDate}</td>
                      <td className="py-2 px-3 text-sm">{borrow.dueDate}</td>
                      <td className="py-2 px-3 text-sm">
                        {borrow.daysOverdue > 0 ? (
                          <span className="px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800">
                            {borrow.daysOverdue} days overdue
                          </span>
                        ) : (
                          <span className="px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                            On time
                          </span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    );
  };

  const renderFinesReport = () => {
    if (!finesReport || (user.role !== "ADMIN" && user.role !== "LIBRARIAN")) return null;
    
    // Create months array for chart
    const months = [
      "January", "February", "March", "April", "May", "June",
      "July", "August", "September", "October", "November", "December"
    ];
    
    return (
      <div className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-green-50 p-4 rounded-lg shadow">
            <h3 className="text-lg font-medium text-green-800">Total Fines Collected</h3>
            <p className="text-3xl font-bold text-green-600">{formatCurrency(finesReport.totalFinesCollected)}</p>
          </div>
          <div className="bg-yellow-50 p-4 rounded-lg shadow">
            <h3 className="text-lg font-medium text-yellow-800">Outstanding Fines</h3>
            <p className="text-3xl font-bold text-yellow-600">{formatCurrency(finesReport.outstandingFines)}</p>
          </div>
        </div>
        
        <div>
          <h3 className="text-xl font-semibold text-gray-700 mb-3">Fines Collected by Month</h3>
          <div className="bg-white rounded-lg shadow p-4">
            <div className="flex flex-col space-y-2">
              {Object.entries(finesReport.finesByMonth || {}).sort(([a], [b]) => Number(a) - Number(b)).map(([month, amount]) => (
                <div key={month} className="flex items-center">
                  <div className="w-32 font-medium text-gray-700">{months[Number(month) - 1]}</div>
                  <div className="flex-1">
                    <div className="relative h-6 bg-gray-100 rounded-full overflow-hidden">
                      <div 
                        className="absolute top-0 left-0 h-full bg-blue-500"
                        style={{ width: `${(amount / finesReport.totalFinesCollected) * 100}%` }}
                      ></div>
                    </div>
                  </div>
                  <div className="w-24 pl-4 text-right">{formatCurrency(amount)}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderPopularBooksReport = () => {
    if (!popularBooks || popularBooks.length === 0) return null;
    
    return (
      <div>
        <h3 className="text-xl font-semibold text-gray-700 mb-3">Most Popular Books</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {popularBooks.map((item) => (
            <div key={item.book.id} className="bg-white rounded-lg shadow p-4 hover:shadow-md transition duration-200">
              <h4 className="text-lg font-semibold text-blue-600">{item.book.title}</h4>
              <p className="text-sm text-gray-500">by {item.book.author}</p>
              <div className="flex items-center mt-3 text-gray-700">
                <span className="text-2xl font-bold text-indigo-600 mr-2">{item.issueCount}</span>
                <span>times borrowed</span>
              </div>
              {item.book.isbn && (
                <p className="mt-2 text-xs text-gray-500">ISBN: {item.book.isbn}</p>
              )}
            </div>
          ))}
        </div>
      </div>
    );
  };

  return (
    <div className="bg-white rounded-xl shadow-lg p-6">
      <h2 className="text-xl font-bold text-gray-800 mb-6">Library Reports & Analytics</h2>
      
      <div className="flex border-b border-gray-200 mb-6">
        <button
          className={`py-2 px-4 border-b-2 font-medium text-sm focus:outline-none ${
            activeTab === "activity"
              ? "border-blue-500 text-blue-600"
              : "border-transparent hover:border-gray-300 text-gray-500 hover:text-gray-700"
          }`}
          onClick={() => setActiveTab("activity")}
        >
          My Activity
        </button>
        {(user.role === "ADMIN" || user.role === "LIBRARIAN") && (
          <button
            className={`py-2 px-4 border-b-2 font-medium text-sm focus:outline-none ${
              activeTab === "fines"
                ? "border-blue-500 text-blue-600"
                : "border-transparent hover:border-gray-300 text-gray-500 hover:text-gray-700"
            }`}
            onClick={() => setActiveTab("fines")}
          >
            Fines Report
          </button>
        )}
        <button
          className={`py-2 px-4 border-b-2 font-medium text-sm focus:outline-none ${
            activeTab === "popular"
              ? "border-blue-500 text-blue-600"
              : "border-transparent hover:border-gray-300 text-gray-500 hover:text-gray-700"
          }`}
          onClick={() => setActiveTab("popular")}
        >
          Popular Books
        </button>
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
        <div>
          {activeTab === "activity" && renderActivityReport()}
          {activeTab === "fines" && renderFinesReport()}
          {activeTab === "popular" && renderPopularBooksReport()}
        </div>
      )}
    </div>
  );
}
