import React, { useState, useEffect } from 'react';

export default function AssetForm({ onSubmit, initialData, onCancel }) {
  const [formData, setFormData] = useState({
    name: '',
    category: 'Laptop',
    serialNumber: '',
    assignedTo: '',
    status: 'Available',
    purchaseDate: ''
  });

  useEffect(() => {
    if (initialData) setFormData(initialData);
  }, [initialData]);

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px', maxWidth: '500px' }}>
      <h3>{initialData ? 'Edit Asset' : 'Add New Asset'}</h3>
      <input type="text" placeholder="Asset Name" required value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} style={{ padding: '8px' }} />
      <select value={formData.category} onChange={e => setFormData({...formData, category: e.target.value})} style={{ padding: '8px' }}>
        <option value="Laptop">Laptop</option>
        <option value="Desktop">Desktop</option>
        <option value="Server">Server</option>
        <option value="Mobile">Mobile</option>
      </select>
      <input type="text" placeholder="Serial Number" required value={formData.serialNumber} onChange={e => setFormData({...formData, serialNumber: e.target.value})} style={{ padding: '8px' }} />
      <input type="text" placeholder="Assigned To" required value={formData.assignedTo} onChange={e => setFormData({...formData, assignedTo: e.target.value})} style={{ padding: '8px' }} />
      <select value={formData.status} onChange={e => setFormData({...formData, status: e.target.value})} style={{ padding: '8px' }}>
        <option value="Available">Available</option>
        <option value="In Use">In Use</option>
        <option value="Under Maintenance">Under Maintenance</option>
      </select>
      <input type="date" required value={formData.purchaseDate} onChange={e => setFormData({...formData, purchaseDate: e.target.value})} style={{ padding: '8px' }} />
      <div style={{ display: 'flex', gap: '10px' }}>
        <button type="submit" style={{ padding: '8px 16px', backgroundColor: '#007bff', color: '#fff', border: 'none' }}>Save Asset</button>
        <button type="button" onClick={onCancel} style={{ padding: '8px 16px' }}>Cancel</button>
      </div>
    </form>
  );
}