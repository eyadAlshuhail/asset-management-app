import React, { useState, useEffect, useCallback } from 'react';
import AssetList from './components/AssetList';
import AssetForm from './components/AssetForm';
import About from './components/About';
import Pagination from './components/Pagination';
import * as api from './api/assetApi';

export default function App() {
  const [currentPage, setCurrentPage] = useState('assets');
  const [editingAsset, setEditingAsset] = useState(null);

  // server-side pagination state
  const [assets, setAssets] = useState([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(5);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // search by id state (null = not searching)
  const [searchId, setSearchId] = useState('');
  const [searchResult, setSearchResult] = useState(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const loadAssets = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await api.getAssets(page, size);
      setAssets(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (err) {
      setError(`Could not load assets: ${err.message}`);
    } finally {
      setLoading(false);
    }
  }, [page, size]);

  // reload whenever the list view is shown or the page/size changes
  useEffect(() => {
    if (currentPage === 'assets') loadAssets();
  }, [currentPage, loadAssets]);

  // if we deleted the last row of a page, step back one page
  useEffect(() => {
    if (!loading && assets.length === 0 && page > 0 && page >= totalPages) {
      setPage(Math.max(totalPages - 1, 0));
    }
  }, [assets, loading, page, totalPages]);

  const goToList = () => {
    setEditingAsset(null);
    setCurrentPage('assets');
  };

  const handleAddOrUpdate = async (assetData) => {
    setError('');
    try {
      if (editingAsset) {
        await api.updateAsset(editingAsset.id, assetData);
      } else {
        await api.createAsset(assetData);
      }
      setSearchResult(null);
      setSearchId('');
      goToList();
    } catch (err) {
      setError(`Could not save asset: ${err.message}`);
    }
  };

  const handleDelete = async (id) => {
    setError('');
    try {
      await api.deleteAsset(id);
      setSearchResult(null);
      await loadAssets();
    } catch (err) {
      setError(`Could not delete asset: ${err.message}`);
    }
  };

  const handleSearchById = async (e) => {
    e.preventDefault();
    if (!searchId.trim()) return;
    setError('');
    try {
      const asset = await api.getAssetById(searchId.trim());
      setSearchResult(asset);
    } catch (err) {
      setSearchResult(null);
      setError(err.status === 404 ? `No asset found with ID ${searchId}` : `Search failed: ${err.message}`);
    }
  };

  const clearSearch = () => {
    setSearchId('');
    setSearchResult(null);
    setError('');
  };

  const handleSizeChange = (newSize) => {
    setSize(newSize);
    setPage(0);
  };

  return (
    <div style={{ fontFamily: 'Arial, sans-serif', padding: '20px', maxWidth: '1100px', margin: '0 auto' }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '2px solid #333', paddingBottom: '10px' }}>
        <h2>IT Asset Management System</h2>
        <nav style={{ display: 'flex', gap: '15px' }}>
          <button onClick={goToList}>Asset List</button>
          <button onClick={() => { setEditingAsset(null); setCurrentPage('add'); }}>Add Asset</button>
          <button onClick={() => setCurrentPage('about')}>About Team</button>
        </nav>
      </header>

      <main style={{ marginTop: '20px' }}>
        {error && (
          <div role="alert" style={{ padding: '10px', marginBottom: '15px', backgroundColor: '#fdecea', color: '#a12622', border: '1px solid #f5c2c0' }}>
            {error}
          </div>
        )}

        {currentPage === 'assets' && (
          <div>
            <form onSubmit={handleSearchById} style={{ display: 'flex', gap: '10px', marginBottom: '15px' }}>
              <input
                type="number"
                min="1"
                placeholder="Search by asset ID..."
                value={searchId}
                onChange={(e) => setSearchId(e.target.value)}
                style={{ flex: 1, padding: '10px', fontSize: '14px' }}
              />
              <button type="submit">Search</button>
              {(searchResult || searchId) && <button type="button" onClick={clearSearch}>Clear</button>}
            </form>

            {searchResult ? (
              <AssetList
                assets={[searchResult]}
                onEdit={(asset) => { setEditingAsset(asset); setCurrentPage('add'); }}
                onDelete={handleDelete}
              />
            ) : (
              <>
                {loading ? <p>Loading...</p> : (
                  <AssetList
                    assets={assets}
                    onEdit={(asset) => { setEditingAsset(asset); setCurrentPage('add'); }}
                    onDelete={handleDelete}
                  />
                )}
                <Pagination
                  page={page}
                  size={size}
                  totalPages={totalPages}
                  totalElements={totalElements}
                  onPageChange={setPage}
                  onSizeChange={handleSizeChange}
                />
              </>
            )}
          </div>
        )}

        {currentPage === 'add' && (
          <AssetForm
            onSubmit={handleAddOrUpdate}
            initialData={editingAsset}
            onCancel={goToList}
          />
        )}

        {currentPage === 'about' && <About />}
      </main>
    </div>
  );
}
