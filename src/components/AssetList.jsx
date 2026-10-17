import React from 'react';

export default function AssetList({ assets, onEdit, onDelete }) {
  return (
    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
      <thead>
        <tr style={{ backgroundColor: '#f4f4f4', borderBottom: '2px solid #ccc' }}>
          <th style={{ padding: '10px' }}>Name</th>
          <th>Category</th>
          <th>Serial Number</th>
          <th>Assigned To</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {assets.length === 0 ? (
          <tr><td colSpan="6" style={{ padding: '20px', textAlign: 'center' }}>No assets found.</td></tr>
        ) : (
          assets.map(asset => (
            <tr key={asset.id} style={{ borderBottom: '1px solid #ddd' }}>
              <td style={{ padding: '10px' }}>{asset.name}</td>
              <td>{asset.category}</td>
              <td>{asset.serialNumber}</td>
              <td>{asset.assignedTo}</td>
              <td>{asset.status}</td>
              <td>
                <button onClick={() => onEdit(asset)} style={{ marginRight: '8px' }}>Edit</button>
                <button onClick={() => onDelete(asset.id)} style={{ color: 'red' }}>Delete</button>
              </td>
            </tr>
          ))
        )}
      </tbody>
    </table>
  );
}