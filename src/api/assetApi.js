// All HTTP calls to the Spring Boot backend live in this one module,
// so components never call fetch() directly.

export const BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/assets';

async function request(url, options = {}) {
  const response = await fetch(url, options);

  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const body = await response.json();
      if (body.message) message = body.message;
      if (body.fieldErrors) message += ': ' + Object.values(body.fieldErrors).join(', ');
    } catch (_) {
      // response had no JSON body; keep the default message
    }
    const error = new Error(message);
    error.status = response.status;
    throw error;
  }

  // 204 No Content (delete) has no body
  return response.status === 204 ? null : response.json();
}

function jsonOptions(method, data) {
  return {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  };
}

/** GET /api/assets?page=&size=&sort=id,asc  -> { content, page, size, totalElements, totalPages, first, last } */
export function getAssets(page = 0, size = 5) {
  return request(`${BASE_URL}?page=${page}&size=${size}&sort=id,asc`);
}

/** GET /api/assets/{id} */
export function getAssetById(id) {
  return request(`${BASE_URL}/${encodeURIComponent(id)}`);
}

/** POST /api/assets */
export function createAsset(asset) {
  return request(BASE_URL, jsonOptions('POST', asset));
}

/** PUT /api/assets/{id} */
export function updateAsset(id, asset) {
  return request(`${BASE_URL}/${encodeURIComponent(id)}`, jsonOptions('PUT', asset));
}

/** DELETE /api/assets/{id} */
export function deleteAsset(id) {
  return request(`${BASE_URL}/${encodeURIComponent(id)}`, { method: 'DELETE' });
}
