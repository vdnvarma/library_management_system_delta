import React, { useState, useEffect } from "react";
import { getAllUsers, updateUserRole, getUsersByRole, deleteUser } from "../api/api";

export default function UserManagement({ user }) {
  const [users, setUsers] = useState([]);
  const [selectedRole, setSelectedRole] = useState("ALL");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  
  // Fetch users on component mount and when selected role changes
  useEffect(() => {
    const fetchUsers = async () => {
      setLoading(true);
      try {
        let result;
        if (selectedRole === "ALL") {
          result = await getAllUsers();
        } else {
          result = await getUsersByRole(selectedRole);
        }
        
        if (!result.error) {
          setUsers(Array.isArray(result) ? result : []);
          setError("");
        } else {
          setError(result.error);
          setUsers([]);
        }
      } catch (err) {
        setError("Failed to fetch users");
        setUsers([]);
      } finally {
        setLoading(false);
      }
    };
    
    fetchUsers();
  }, [selectedRole]);
  
  const handleRoleChange = async (userId, newRole) => {
    try {
      const result = await updateUserRole(userId, newRole);
      if (!result.error) {
        setUsers(users.map(user => 
          user.id === userId ? { ...user, role: newRole } : user
        ));
        setError("");
      } else {
        setError(result.error);
      }
    } catch (err) {
      setError("Failed to update role");
    }
  };
  
  const handleDeleteUser = async (userId) => {
    if (window.confirm("Are you sure you want to delete this user?")) {
      try {
        const result = await deleteUser(userId);
        if (!result.error) {
          setUsers(users.filter(user => user.id !== userId));
          setError("");
        } else {
          setError(result.error);
        }
      } catch (err) {
        setError("Failed to delete user");
      }
    }
  };
  
  // Only administrators should have access to this page
  if (user.role !== "ADMIN") {
    return (
      <div className="bg-yellow-100 border border-yellow-400 text-yellow-800 px-4 py-3 rounded">
        <p>You don't have permission to access user management.</p>
      </div>
    );
  }
  
  return (
    <div className="bg-white rounded-xl shadow-lg p-6">
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between mb-6">
        <h2 className="text-xl font-bold text-gray-800 mb-4 md:mb-0">User Management</h2>
        <div className="flex items-center space-x-2">
          <label htmlFor="role-filter" className="text-gray-600">Filter by Role:</label>
          <select
            id="role-filter"
            value={selectedRole}
            onChange={(e) => setSelectedRole(e.target.value)}
            className="border border-gray-300 rounded px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="ALL">All Users</option>
            <option value="ADMIN">Administrators</option>
            <option value="LIBRARIAN">Librarians</option>
            <option value="STUDENT">Students</option>
          </select>
        </div>
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
          <table className="min-w-full bg-white">
            <thead className="bg-gray-50 border-b">
              <tr>
                <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Name</th>
                <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Username</th>
                <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Role</th>
                <th className="text-left py-3 px-4 font-semibold text-sm text-gray-700">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {users.map(user => (
                <tr key={user.id} className="hover:bg-gray-50 transition duration-150">
                  <td className="py-3 px-4 text-sm">{user.name}</td>
                  <td className="py-3 px-4 text-sm">{user.username}</td>
                  <td className="py-3 px-4">
                    <select
                      value={user.role}
                      onChange={(e) => handleRoleChange(user.id, e.target.value)}
                      className="border border-gray-300 rounded px-2 py-1 text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    >
                      <option value="ADMIN">Administrator</option>
                      <option value="LIBRARIAN">Librarian</option>
                      <option value="STUDENT">Student</option>
                    </select>
                  </td>
                  <td className="py-3 px-4">
                    <button
                      onClick={() => handleDeleteUser(user.id)}
                      className="bg-red-600 hover:bg-red-700 text-white text-xs py-1 px-2 rounded transition duration-200"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
              {users.length === 0 && (
                <tr>
                  <td colSpan="4" className="py-6 px-4 text-center text-gray-500">
                    No users found
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
