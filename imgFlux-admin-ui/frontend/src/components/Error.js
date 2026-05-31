import React from 'react';
import {Alert, Container} from 'react-bootstrap';
import {Link} from 'react-router-dom';

const Error = () => {
  return (
      <Container className="d-flex justify-content-center align-items-center" style={{minHeight: '100vh'}}>
        <Alert variant="danger" className="text-center">
          <Alert.Heading>Error</Alert.Heading>
          <p>An error occurred. Please try again later.</p>
          <hr/>
          <div className="d-flex justify-content-center">
            <Link to="/images" className="btn btn-primary me-2">Go to Image List</Link>
            <Link to="/login" className="btn btn-secondary">Back to Login</Link>
          </div>
        </Alert>
      </Container>
  );
};

export default Error;
