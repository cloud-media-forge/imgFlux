import React, {useState} from 'react';
import {Alert, Button, Card, Container, Form} from 'react-bootstrap';
import {Link} from 'react-router-dom';
import axios from 'axios';

const Login = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // Using form data for login
      const loginData = new FormData();
      loginData.append('username', formData.username);
      loginData.append('password', formData.password);

      const response = await axios.post('/admin/login', loginData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        withCredentials: true
      });

      if (response.status === 200) {
        window.location.href = '/dashboard';
      }
    } catch (err) {
      setError('Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
      <Container className="d-flex justify-content-center align-items-center" style={{minHeight: '100vh'}}>
        <Card style={{width: '400px'}}>
          <Card.Header className="text-center">
            <h3>Login</h3>
          </Card.Header>
          <Card.Body>
            {error && <Alert variant="danger">{error}</Alert>}
            <Form onSubmit={handleSubmit}>
              <Form.Group className="mb-3">
                <Form.Label>Username</Form.Label>
                <Form.Control
                    type="text"
                    name="username"
                    value={formData.username}
                    onChange={handleChange}
                    required
                />
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>Password</Form.Label>
                <Form.Control
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                />
              </Form.Group>
              <Button variant="primary" type="submit" className="w-100" disabled={loading}>
                {loading ? 'Logging in...' : 'Login'}
              </Button>
            </Form>
            <div className="text-center mt-3">
              <Link to="/register">Don't have an account? Register here</Link>
            </div>
          </Card.Body>
        </Card>
      </Container>
  );
};

export default Login;
