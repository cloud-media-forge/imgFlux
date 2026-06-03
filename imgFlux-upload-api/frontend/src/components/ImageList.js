import React, {useEffect, useState} from 'react';
import {Alert, Breadcrumb, Button, Col, Container, Form, Modal, Row, Table} from 'react-bootstrap';
import {Link, useLocation, useNavigate} from 'react-router-dom';
import axios from 'axios';

const ImageList = () => {
  const [images, setImages] = useState([]);
  const [currentPath, setCurrentPath] = useState('');
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [showCreateFolderModal, setShowCreateFolderModal] = useState(false);
  const [showResultModal, setShowResultModal] = useState(false);
  const [resultMessage, setResultMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [uploadData, setUploadData] = useState({
    file: null,
    processImage: false,
    width: 0,
    height: 0,
    quality: 80,
    format: '',
    extent: false,
    trim: false
  });
  const [folderName, setFolderName] = useState('');

  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const pathParam = new URLSearchParams(location.search).get('path') || '';
    setCurrentPath(pathParam);
    fetchImages(pathParam);
  }, [location.search]);

  const fetchImages = async (path) => {
    setLoading(true);
    try {
      const response = await axios.get(`/admin/images?path=${encodeURIComponent(path)}`, {
        withCredentials: true
      });
      setImages(response.data.images || []);
    } catch (error) {
      console.error('Failed to fetch images:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleUploadChange = (e) => {
    const {name, value, type, checked, files} = e.target;
    setUploadData({
      ...uploadData,
      [name]: type === 'checkbox' ? checked : type === 'file' ? files[0] : value
    });
  };

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!uploadData.file) {
      window.alert('Please select a file to upload');
      return;
    }

    const formData = new FormData();
    formData.append('file', uploadData.file);
    if (currentPath) {
      formData.append('path', currentPath);
    }

    if (uploadData.processImage) {
      formData.append('width', uploadData.width);
      formData.append('height', uploadData.height);
      formData.append('quality', uploadData.quality);
      formData.append('extent', uploadData.extent);
      formData.append('trim', uploadData.trim);
      formData.append('format', uploadData.format);
    }

    try {
      const url = uploadData.processImage ? '/api/upload' : '/api/upload/raw';
      const response = await axios.post(url, formData, {
        headers: {'Content-Type': 'multipart/form-data'},
        withCredentials: true
      });
      setResultMessage(<Alert variant="success">{response.data}</Alert>);
      setShowUploadModal(false);
      setShowResultModal(true);
      fetchImages(currentPath);
    } catch (error) {
      setResultMessage(<Alert variant="danger">Upload failed: {error.message}</Alert>);
      setShowUploadModal(false);
      setShowResultModal(true);
    }
  };

  const handleCreateFolder = async (e) => {
    e.preventDefault();
    if (!folderName) {
      window.alert('Please enter a folder name');
      return;
    }

    const formData = new FormData();
    formData.append('folderName', folderName);
    if (currentPath) {
      formData.append('path', currentPath);
    }

    try {
      const response = await axios.post('/admin/images/create-folder', formData, {
        headers: {'Content-Type': 'multipart/form-data'},
        withCredentials: true
      });
      setResultMessage(<Alert variant="success">{response.data}</Alert>);
      setShowCreateFolderModal(false);
      setShowResultModal(true);
      setFolderName('');
      fetchImages(currentPath);
    } catch (error) {
      setResultMessage(<Alert variant="danger">Failed to create folder: {error.message}</Alert>);
      setShowCreateFolderModal(false);
      setShowResultModal(true);
    }
  };

  const handleDelete = async (objectName) => {
    if (!window.confirm(`Are you sure you want to delete ${objectName}?`)) {
      return;
    }

    const formData = new FormData();
    formData.append('objectName', objectName);

    try {
      const response = await axios.delete('/admin/images/delete', {
        data: formData,
        headers: {'Content-Type': 'multipart/form-data'},
        withCredentials: true
      });
      setResultMessage(<Alert variant="success">{response.data}</Alert>);
      setShowResultModal(true);
      fetchImages(currentPath);
    } catch (error) {
      setResultMessage(<Alert variant="danger">Failed to delete object: {error.message}</Alert>);
      setShowResultModal(true);
    }
  };

  const navigateToFolder = (folderPath) => {
    navigate(`/video?path=${encodeURIComponent(folderPath)}`);
  };

  const navigateToHome = () => {
    navigate('/video');
  };

  const formatFileSize = (bytes) => {
    if (!bytes) return '-';
    return `${(bytes / 1024).toFixed(2)} KB`;
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return <div className="text-center mt-5">Loading...</div>;
  }

  return (
      <Container fluid className="mt-4">
        <Row>
          <Col md={3}>
            <div className="list-group">
              <Link to="/dashboard" className="list-group-item list-group-item-action">Dashboard</Link>
              <Link to="/video" className="list-group-item list-group-item-action active">My Images</Link>
            </div>
          </Col>
          <Col md={9}>
            <div className="d-flex justify-content-between align-items-center mb-3">
              <div>
                <h2>My Images</h2>
                <Breadcrumb>
                  <Breadcrumb.Item onClick={navigateToHome}>Home</Breadcrumb.Item>
                  {currentPath && <Breadcrumb.Item active>{currentPath}</Breadcrumb.Item>}
                </Breadcrumb>
              </div>
              <div>
                <Button variant="success me-2" onClick={() => setShowCreateFolderModal(true)}>
                  Create Folder
                </Button>
                <Button variant="primary" onClick={() => setShowUploadModal(true)}>
                  Upload New Video
                </Button>
              </div>
            </div>

            <div className="table-responsive">
              <Table striped>
                <thead>
                <tr>
                  <th>Name</th>
                  <th>Size</th>
                  <th>Last Modified</th>
                  <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {images.map((image, index) => (
                    <tr key={index}>
                      <td>
                        {image.folder ? (
                            <span
                                style={{cursor: 'pointer', color: 'blue'}}
                                onClick={() => navigateToFolder(image.fullPath)}
                            >
                          {image.fileName}
                        </span>
                        ) : (
                            <a
                                href={`/admin/images/${image.fullPath}`}
                                target="_blank"
                                rel="noopener noreferrer"
                            >
                              {image.fileName}
                            </a>
                        )}
                      </td>
                      <td>{image.folder ? '-' : formatFileSize(image.fileSize)}</td>
                      <td>{formatDate(image.lastModified)}</td>
                      <td>
                        <Button
                            variant="danger"
                            size="sm"
                            onClick={() => handleDelete(image.fullPath)}
                        >
                          Delete
                        </Button>
                      </td>
                    </tr>
                ))}
                {images.length === 0 && (
                    <tr>
                      <td colSpan="4" className="text-center">No Files found</td>
                    </tr>
                )}
                </tbody>
              </Table>
            </div>
          </Col>
        </Row>

        {/* Upload Modal */}
        <Modal show={showUploadModal} onHide={() => setShowUploadModal(false)}>
          <Modal.Header closeButton>
            <Modal.Title>Upload New Video</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Form onSubmit={handleUpload}>
              <Form.Group className="mb-3">
                <Form.Label>Select Video</Form.Label>
                <Form.Control
                    type="file"
                    name="file"
                    onChange={handleUploadChange}
                    accept="video/*,image/*"
                    required
                />
                <Form.Text>Supported formats: MP4, AVI, MOV, JPG, JPEG, PNG, GIF (Max 10MB)</Form.Text>
              </Form.Group>

              <Form.Check
                  type="checkbox"
                  id="processImage"
                  name="processImage"
                  label="Process video/image (resize, quality, format conversion)"
                  checked={uploadData.processImage}
                  onChange={handleUploadChange}
                  className="mb-3"
              />

              {uploadData.processImage && (
                  <div>
                    <Row>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label>Width (px)</Form.Label>
                          <Form.Control
                              type="number"
                              name="width"
                              value={uploadData.width}
                              onChange={handleUploadChange}
                              min="0"
                          />
                        </Form.Group>
                      </Col>
                      <Col md={6}>
                        <Form.Group className="mb-3">
                          <Form.Label>Height (px)</Form.Label>
                          <Form.Control
                              type="number"
                              name="height"
                              value={uploadData.height}
                              onChange={handleUploadChange}
                              min="0"
                          />
                        </Form.Group>
                      </Col>
                    </Row>

                    <Form.Group className="mb-3">
                      <Form.Label>Quality (1-100)</Form.Label>
                      <Form.Control
                          type="number"
                          name="quality"
                          value={uploadData.quality}
                          onChange={handleUploadChange}
                          min="1"
                          max="100"
                      />
                    </Form.Group>

                    <Form.Group className="mb-3">
                      <Form.Label>Target Format</Form.Label>
                      <Form.Select
                          name="format"
                          value={uploadData.format}
                          onChange={handleUploadChange}
                      >
                        <option value="">Keep Original</option>
                        <option value="JPG">JPG</option>
                        <option value="JPEG">JPEG</option>
                        <option value="PNG">PNG</option>
                        <option value="GIF">GIF</option>
                        <option value="AVIF">AVIF</option>
                      </Form.Select>
                    </Form.Group>

                    <Form.Check
                        type="checkbox"
                        id="extent"
                        name="extent"
                        label="Extend image to exact dimensions"
                        checked={uploadData.extent}
                        onChange={handleUploadChange}
                        className="mb-3"
                    />

                    <Form.Check
                        type="checkbox"
                        id="trim"
                        name="trim"
                        label="Trim image"
                        checked={uploadData.trim}
                        onChange={handleUploadChange}
                        className="mb-3"
                    />
                  </div>
              )}

              <Button variant="secondary" onClick={() => setShowUploadModal(false)} className="me-2">
                Cancel
              </Button>
              <Button variant="primary" type="submit">
                Upload Video
              </Button>
            </Form>
          </Modal.Body>
        </Modal>

        {/* Create Folder Modal */}
        <Modal show={showCreateFolderModal} onHide={() => setShowCreateFolderModal(false)}>
          <Modal.Header closeButton>
            <Modal.Title>Create New Folder</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Form onSubmit={handleCreateFolder}>
              <Form.Group className="mb-3">
                <Form.Label>Folder Name</Form.Label>
                <Form.Control
                    type="text"
                    value={folderName}
                    onChange={(e) => setFolderName(e.target.value)}
                    required
                />
              </Form.Group>
              <Button variant="secondary" onClick={() => setShowCreateFolderModal(false)} className="me-2">
                Cancel
              </Button>
              <Button variant="primary" type="submit">
                Create Folder
              </Button>
            </Form>
          </Modal.Body>
        </Modal>

        {/* Result Modal */}
        <Modal show={showResultModal} onHide={() => setShowResultModal(false)}>
          <Modal.Header closeButton>
            <Modal.Title>Result</Modal.Title>
          </Modal.Header>
          <Modal.Body>{resultMessage}</Modal.Body>
          <Modal.Footer>
            <Button variant="primary" onClick={() => setShowResultModal(false)}>
              OK
            </Button>
          </Modal.Footer>
        </Modal>
      </Container>
  );
};

export default ImageList;
