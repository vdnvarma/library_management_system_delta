import React, { useState } from "react";
import { register } from "../api/api";

export default function Register({ onRegister }) {
  const [name, setName] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async e => {
    e.preventDefault();
    const user = await register(name, username, password);
    if (user && user.id) {
      onRegister(user);
    } else {
      setError("Registration failed");
    }
  };

  return (
    <form onSubmit={handleSubmit} className="max-w-md mx-auto bg-white rounded-xl shadow-lg p-8">
      <h2 className="text-3xl font-bold mb-8 text-center text-gray-800">Register</h2>
      {error && <div className="text-red-500 text-sm mb-4 p-3 bg-red-50 rounded-lg border border-red-200">{error}</div>}
      <div className="space-y-4">
        <input 
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent transition duration-200" 
          value={name} 
          onChange={e=>setName(e.target.value)} 
          placeholder="Full Name" 
          required 
        />
        <input 
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent transition duration-200" 
          value={username} 
          onChange={e=>setUsername(e.target.value)} 
          placeholder="Username" 
          required 
        />
        <input 
          type="password" 
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent transition duration-200" 
          value={password} 
          onChange={e=>setPassword(e.target.value)} 
          placeholder="Password" 
          required 
        />
        <button 
          type="submit" 
          className="w-full bg-green-600 text-white py-3 px-6 rounded-lg font-medium hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 transition duration-200 transform hover:scale-105"
        >
          Register
        </button>
      </div>
    </form>
  );
} 