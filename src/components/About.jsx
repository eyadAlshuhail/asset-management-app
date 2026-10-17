import React from 'react';

export default function About() {
  const teamMembers = [
    { name: 'Mohammed Al-Jarbou', id: '221110338' },
    { name: 'Eyad Al-Shuhail', id: '222110093' },
    { name: 'Hamad Al-Waheebi', id: '222110607' },
    { name: 'Ahmad Al-Talhi', id: '222111457' }
  ];

  return (
    <div style={{ padding: '10px' }}>
      <h3>Project Team Members</h3>
      <ul>
        {teamMembers.map((m, idx) => (
          <li key={idx} style={{ marginBottom: '8px' }}>
            <strong>{m.name}</strong> - Student ID: {m.id}
          </li>
        ))}
      </ul>
    </div>
  );
}