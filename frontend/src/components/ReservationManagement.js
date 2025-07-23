import React, { useState, useEffect } from "react";
import { getAllReservations, getUserReservations, cancelReservation, notifyReservation } from "../api/api";

export default function ReservationManagement({ user }) {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [viewMode, setViewMode] = useState(user.role === "STUDENT" ? "my" : "all");

  useEffect(() => {
    const fetchReservations = async () => {
      setLoading(true);
      setError("");
      
      try {
        let result;
        if (viewMode === "my" || user.role === "STUDENT") {
          result = await getUserReservations(user.id);
        } else {
          result = await getAllReservations();
        }
        
        if (result.error) {
          setError(result.error);
          setReservations([]);
        } else {
          setReservations(Array.isArray(result) ? result : []);
        }
      } catch (err) {
        setError("Failed to load reservations");
        setReservations([]);
      } finally {
        setLoading(false);
      }
    };
    
    fetchReservations();
  }, [user.id, user.role, viewMode]);
  
  const handleCancel = async (reservationId) => {
    if (window.confirm("Are you sure you want to cancel this reservation?")) {
      try {
        const result = await cancelReservation(reservationId);
        if (!result.error) {
          setReservations(reservations.filter(res => res.id !== reservationId));
          setError("");
        } else {
          setError(result.error);
        }
      } catch (err) {
        setError("Failed to cancel reservation");
      }
    }
  };
  
  const handleNotify = async (reservationId) => {
    try {
      const result = await notifyReservation(reservationId);
      if (!result.error) {
        setReservations(reservations.map(res => 
          res.id === reservationId ? { ...res, notified: true } : res
        ));
        setError("");
      } else {
        setError(result.error);
      }
    } catch (err) {
      setError("Failed to send notification");
    }
  };
  
  return (
    <div className="bg-white rounded-xl shadow-lg p-6">
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between mb-6">
        <h2 className="text-xl font-bold text-gray-800 mb-4 md:mb-0">Book Reservations</h2>
        
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
              All Reservations
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
              My Reservations
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
          {reservations.length === 0 ? (
            <div className="text-center py-8">
              <div className="text-gray-400 text-lg mb-2">📚</div>
              <p className="text-gray-500">No reservations found.</p>
            </div>
          ) : (
            <table className="min-w-full bg-white">
              <thead className="bg-gray-50 border-b">
                <tr>
                  <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Book Title</th>
                  <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Reserved By</th>
                  <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Date</th>
                  <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Status</th>
                  <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {reservations.map(reservation => (
                  <tr key={reservation.id} className="hover:bg-gray-50 transition duration-150">
                    <td className="py-3 px-4 text-sm">{reservation.book.title}</td>
                    <td className="py-3 px-4 text-sm">{reservation.user.name}</td>
                    <td className="py-3 px-4 text-sm">{new Date(reservation.reservationDate).toLocaleDateString()}</td>
                    <td className="py-3 px-4">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        reservation.active 
                          ? reservation.notified
                            ? 'bg-green-100 text-green-800'
                            : 'bg-yellow-100 text-yellow-800'
                          : 'bg-gray-100 text-gray-800'
                      }`}>
                        {reservation.active
                          ? reservation.notified
                            ? 'Notified'
                            : 'Waiting'
                          : 'Inactive'}
                      </span>
                    </td>
                    <td className="py-3 px-4 flex space-x-2">
                      <button
                        onClick={() => handleCancel(reservation.id)}
                        className="bg-red-600 hover:bg-red-700 text-white text-xs py-1 px-2 rounded transition duration-200"
                      >
                        Cancel
                      </button>
                      
                      {(user.role === "ADMIN" || user.role === "LIBRARIAN") && 
                       reservation.active && 
                       !reservation.notified && (
                        <button
                          onClick={() => handleNotify(reservation.id)}
                          className="bg-green-600 hover:bg-green-700 text-white text-xs py-1 px-2 rounded transition duration-200"
                        >
                          Notify
                        </button>
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
  );
}
