import React, {useEffect, useState} from 'react';
import {Container, Nav, Navbar as BootstrapNavbar} from 'react-bootstrap';
import {Link} from 'react-router-dom';

const Navbar = () => {
  const [username, setUsername] = useState('');
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    // Check if user is logged in
    const checkAuth = async () => {
      try {
        const response = await fetch('/admin/user', {
          credentials: 'include'
        });
        if (response.ok) {
          const data = await response.json();
          setUsername(data.username);
          setIsAuthenticated(true);
        }
      } catch (error) {
        console.error('Auth check failed:', error);
      }
    };
    checkAuth();
  }, []);

  const handleLogout = async () => {
    try {
      await fetch('/logout', {
        method: 'POST',
        credentials: 'include'
      });
      window.location.href = '/login';
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  return (
      <BootstrapNavbar bg="dark" variant="dark" expand="lg">
        <Container>
          <BootstrapNavbar.Brand as={Link} to="/dashboard">
            Image Flux Admin
          </BootstrapNavbar.Brand>
          <BootstrapNavbar.Toggle aria-controls="basic-navbar-nav"/>
          <BootstrapNavbar.Collapse id="basic-navbar-nav">
            <Nav className="me-auto">
              <Nav.Link as={Link} to="/dashboard">Dashboard</Nav.Link>
              <Nav.Link as={Link} to="/images">My Images</Nav.Link>
            </Nav>
            {isAuthenticated && (
                <Nav className="ms-auto">
                  <Nav.Item className="nav-link me-3">
                    Welcome, {username}
                  </Nav.Item>
                  <Nav.Link onClick={handleLogout}>Logout</Nav.Link>
                </Nav>
            )}
          </BootstrapNavbar.Collapse>
        </Container>
      </BootstrapNavbar>
  );
};

export default Navbar;
