import React, { useState } from 'react';
import AssetList from './components/AssetList';
import AssetForm from './components/AssetForm';
import About from './components/About';

const initialAssets = [
  { id: '1', name: 'MacBook Pro 16"', category: 'Laptop', serialNumber: 'SN-90812', assignedTo: 'Mohammed Al-Jarbou', status: 'In Use', purchaseDate: '2024-01-15' },
  { id: '2', name: 'Dell XPS 15', category: 'Laptop', serialNumber: 'SN-33211', assignedTo: 'Eyad Al-Shuhail', status: 'In Use', purchaseDate: '2024-03-10' },
  { id: '3', name: 'ThinkPad T14', category: 'Laptop', serialNumber: 'SN-44512', assignedTo: 'Hamad Al-Waheebi', status: 'In Use', purchaseDate: '2024-02-20' },
  { id: '4', name: 'HP ZBook Studio', category: 'Laptop', serialNumber: 'SN-88219', assignedTo: 'Ahmad Al-Talhi', status: 'Available', purchaseDate: '2024-04-05' }
];

export default function App() {
  const [currentPage, setCurrentPage] = useState('assets');
  const [assets, setAssets] = useState(initialAssets);
  const [editingAsset, setEditingAsset] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  const handleAddOrUpdate = (assetData) => {
    if (editingAsset) {
      setAssets(assets.map(a => a.id === editingAsset.id ? { ...assetData, id: a.id } : a));
      setEditingAsset(null);
    } else {
      setAssets([...assets, { ...assetData, id: Date.now().toString() }]);
    }
    setCurrentPage('assets');
  };

  const handleDelete = (id) => {
    setAssets(assets.filter(a => a.id !== id));
  };

  const filteredAssets = assets.filter(a => 
    a.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    a.serialNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
    a.assignedTo.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div style={{ fontFamily: 'Arial, sans-serif', padding: '20px', maxWidth: '1000px', margin: '0 auto' }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '2px solid #333', paddingBottom: '10px' }}>
        <h2>IT Asset Management System</h2>
        <nav style={{ display: 'flex', gap: '15px' }}>
          <button onClick={() => { setCurrentPage('assets'); setEditingAsset(null); }}>Asset List</button>
          <button onClick={() => { setCurrentPage('add'); setEditingAsset(null); }}>Add Asset</button>
          <button onClick={() => setCurrentPage('about')}>About Team</button>
        </nav>
      </header>

      <main style={{ marginTop: '20px' }}>
        {currentPage === 'assets' && (
          <div>
            <input 
              type="text" 
              placeholder="Search by name, serial, or assignee..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{ width: '100%', padding: '10px', marginBottom: '15px', fontSize: '14px' }}
            />
            <AssetList 
              assets={filteredAssets} 
              onEdit={(asset) => { setEditingAsset(asset); setCurrentPage('add'); }} 
              onDelete={handleDelete} 
            />
          </div>
        )}

        {currentPage === 'add' && (
          <AssetForm 
            onSubmit={handleAddOrUpdate} 
            initialData={editingAsset} 
            onCancel={() => { setCurrentPage('assets'); setEditingAsset(null); }} 
          />
        )}

        {currentPage === 'about' && <About />}
      </main>
    </div>
  );
}