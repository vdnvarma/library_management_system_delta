import React, { useState } from "react";
import { login, setJwt } from "../api/api";

export default function Login({ onLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async e => {
    e.preventDefault();
    setError("");
    try {
      console.log("Attempting login for: " + username);
      const user = await login(username, password);
      if (user && user.token) {
        console.log("Login successful, setting JWT token");
        setJwt(user.token);
        onLogin(user);
      } else {
        console.log("Login failed, no token received");
        setError(user?.error || "Invalid credentials");
      }
    } catch (error) {
      console.error("Login error:", error);
      setError("Login failed: " + (error.message || "Unknown error"));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4 max-w-md mx-auto mt-10">
      <h2 className="text-2xl font-bold mb-4 text-center">Login</h2>
      {error && <div className="text-red-500 mb-2">{error}</div>}
      <input className="shadow appearance-none border rounded w-full py-2 px-3 mb-4" value={username} onChange={e=>setUsername(e.target.value)} placeholder="Username" required />
      <input type="password" className="shadow appearance-none border rounded w-full py-2 px-3 mb-4" value={password} onChange={e=>setPassword(e.target.value)} placeholder="Password" required />
      <button type="submit" className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded w-full">Login</button>
    </form>
  );
} 