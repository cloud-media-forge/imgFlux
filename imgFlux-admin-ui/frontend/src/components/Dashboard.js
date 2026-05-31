import React, {useEffect, useState} from 'react';
import {Card, Col, Container, Row, Table} from 'react-bootstrap';
import {Link} from 'react-router-dom';
import axios from 'axios';

const Dashboard = () => {
  const [username, setUsername] = useState('');
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        // Fetch user info
        const userResponse = await axios.get('/admin/user', {withCredentials: true});
        setUsername(userResponse.data.username);

        // Fetch images
        const imagesResponse = await axios.get('/admin/images', {withCredentials: true});
        setImages(imagesResponse.data.images || []);
      } catch (error) {
        console.error('Failed to fetch dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return <div className="text-center mt-5">Loading...</div>;
  }

  return (
      <Container fluid className="mt-4">
        <Row>
          <Col md={3}>
            <div className="list-group">
              <Link to="/dashboard" className="list-group-item list-group-item-action active">Dashboard</Link>
              <Link to="/images" className="list-group-item list-group-item-action">My Images</Link>
            </div>
          </Col>
          <Col md={9}>
            <h2>Dashboard</h2>
            <Row>
              <Col md={4}>
                <Card className="text-white bg-primary mb-3">
                  <Card.Header>Total Images</Card.Header>
                  <Card.Body>
                    <Card.Title>{images.length}</Card.Title>
                  </Card.Body>
                </Card>
              </Col>
              <Col md={4}>
                <Card className="text-white bg-success mb-3">
                  <Card.Header>Company</Card.Header>
                  <Card.Body>
                    <Card.Title>N/A</Card.Title>
                  </Card.Body>
                </Card>
              </Col>
              <Col md={4}>
                <Card className="text-white bg-info mb-3">
                  <Card.Header>Team</Card.Header>
                  <Card.Body>
                    <Card.Title>N/A</Card.Title>
                  </Card.Body>
                </Card>
              </Col>
            </Row>
            <h3>Recent Images</h3>
            <div className="table-responsive">
              <Table striped>
                <thead>
                <tr>
                  <th>Image</th>
                  <th>Name</th>
                  <th>Size</th>
                  <th>Uploaded</th>
                  <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {images.slice(0, 5).map((image, index) => (
                    <tr key={index}>
                      <td>
                        <img
                            src={`/admin/images/${image.fullPath}`}
                            alt="Thumbnail"
                            className="img-thumbnail"
                            style={{width: '50px', height: '50px', objectFit: 'cover'}}
                            onError={(e) => {
                              e.target.src = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiB2aWV3Qm94PSIwIDAgMTAwIDEwMCI+PHJlY3Qgd2lkdGg9IjEwMCIgaGVpZ2h0PSIxMDAiIGZpbGw9IiNjY2MiLz48dGV4dCB4PSI1MCIgeT0iNTAiIGZvbnQtc2l6ZT0iMTIiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGZpbGw9IiM2NjYiPk5vIEltYWdlPC90ZXh0Pjwvc3ZnPg==';
                            }}
                        />
                      </td>
                      <td>{image.fileName}</td>
                      <td>{image.fileSize ? `${(image.fileSize / 1024).toFixed(2)} KB` : 'N/A'}</td>
                      <td>{image.lastModified || 'N/A'}</td>
                      <td>
                        <Link to={`/images/view/${image.id}`} className="btn btn-sm btn-outline-primary">View</Link>
                      </td>
                    </tr>
                ))}
                {images.length === 0 && (
                    <tr>
                      <td colSpan="5" className="text-center">No images found</td>
                    </tr>
                )}
                </tbody>
              </Table>
            </div>
          </Col>
        </Row>
      </Container>
  );
};

export default Dashboard;
