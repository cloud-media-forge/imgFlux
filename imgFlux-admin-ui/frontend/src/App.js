import React from 'react';
import {BrowserRouter as Router, Navigate, Route, Routes} from 'react-router-dom';
import Navbar from './components/Navbar';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import ImageList from './components/ImageList';
import ImageView from './components/ImageView';
import Error from './components/Error';

function App() {
  return (
      <Router>
        <div className="App">
          <Navbar/>
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard"/>}/>
            <Route path="/login" element={<Login/>}/>
            <Route path="/register" element={<Register/>}/>
            <Route path="/dashboard" element={<Dashboard/>}/>
            <Route path="/images" element={<ImageList/>}/>
            <Route path="/images/:path" element={<ImageList/>}/>
            <Route path="/images/view/:id" element={<ImageView/>}/>
            <Route path="/error" element={<Error/>}/>
          </Routes>
        </div>
      </Router>
  );
}

export default App;
