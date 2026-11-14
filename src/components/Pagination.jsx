import React from 'react';

export default function Pagination({ page, totalPages, totalElements, size, onPageChange, onSizeChange }) {
  const lastPage = Math.max(totalPages - 1, 0);

  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '15px' }}>
      <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
        <button onClick={() => onPageChange(page - 1)} disabled={page <= 0}>Previous</button>
        <span>Page {totalPages === 0 ? 0 : page + 1} of {totalPages}</span>
        <button onClick={() => onPageChange(page + 1)} disabled={page >= lastPage}>Next</button>
      </div>
      <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
        <span>{totalElements} assets</span>
        <label>
          Per page{' '}
          <select aria-label="Page size" value={size} onChange={(e) => onSizeChange(Number(e.target.value))}>
            <option value={5}>5</option>
            <option value={10}>10</option>
            <option value={20}>20</option>
          </select>
        </label>
      </div>
    </div>
  );
}
