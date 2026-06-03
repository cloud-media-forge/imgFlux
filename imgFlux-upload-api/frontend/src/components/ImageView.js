import React, {useEffect, useState} from 'react';
import {Card, Col, Container, Row, Table} from 'react-bootstrap';
import {Link, useParams} from 'react-router-dom';
import axios from 'axios';

const ImageView = () => {
  const {id} = useParams();
  const [image, setImage] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchImage = async () => {
      try {
        const response = await axios.get(`/admin/images/${id}`, {
          withCredentials: true
        });
        setImage(response.data.image);
      } catch (error) {
        console.error('Failed to fetch image:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchImage();
  }, [id]);

  if (loading) {
    return <div className="text-center mt-5">Loading...</div>;
  }

  if (!image) {
    return <div className="text-center mt-5">Image not found</div>;
  }

  return (
      <Container fluid className="mt-4">
        <Row>
          <Col md={3}>
            <div className="list-group">
              <Link to="/dashboard" className="list-group-item list-group-item-action">Dashboard</Link>
              <Link to="/images" className="list-group-item list-group-item-action active">My Images</Link>
            </div>
          </Col>
          <Col md={9}>
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h2>Image Details</h2>
              <Link to="/images" className="btn btn-secondary">Back to List</Link>
            </div>

            <Row>
              <Col md={8}>
                <Card className="mb-3">
                  <Card.Body className="text-center">
                    <img
                        src={image.filePath}
                        alt={image.fileName}
                        style={{maxWidth: '100%', maxHeight: '600px'}}
                    />
                  </Card.Body>
                </Card>
              </Col>
              <Col md={4}>
                <Card>
                  <Card.Header>Image Information</Card.Header>
                  <Card.Body>
                    <Table bordered>
                      <tbody>
                      <tr>
                        <td><strong>File Name</strong></td>
                        <td>{image.fileName}</td>
                      </tr>
                      <tr>
                        <td><strong>File Size</strong></td>
                        <td>{image.fileSize ? `${(image.fileSize / 1024).toFixed(2)} KB` : 'N/A'}</td>
                      </tr>
                      <tr>
                        <td><strong>Dimensions</strong></td>
                        <td>{image.width} x {image.height}</td>
                      </tr>
                      <tr>
                        <td><strong>Content Type</strong></td>
                        <td>{image.contentType}</td>
                      </tr>
                      <tr>
                        <td><strong>Created At</strong></td>
                        <td>{image.createdAt}</td>
                      </tr>
                      <tr>
                        <td><strong>File Hash</strong></td>
                        <td><small>{image.fileHash}</small></td>
                      </tr>
                      </tbody>
                    </Table>
                  </Card.Body>
                </Card>
              </Col>
            </Row>
          </Col>
        </Row>
      </Container>
  );
};

export default ImageView;
