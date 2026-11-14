import { BASE_URL, getAssets, getAssetById, createAsset, updateAsset, deleteAsset } from './assetApi';

const jsonResponse = (status, body) => ({
  ok: status >= 200 && status < 300,
  status,
  json: () => Promise.resolve(body),
});

beforeEach(() => {
  global.fetch = jest.fn();
});

describe('assetApi', () => {
  it('getAssets calls the paginated endpoint', async () => {
    fetch.mockResolvedValue(jsonResponse(200, { content: [], page: 2 }));
    const data = await getAssets(2, 10);
    expect(fetch).toHaveBeenCalledWith(`${BASE_URL}?page=2&size=10&sort=id,asc`, {});
    expect(data.page).toBe(2);
  });

  it('getAssetById calls /{id}', async () => {
    fetch.mockResolvedValue(jsonResponse(200, { id: 7 }));
    await getAssetById(7);
    expect(fetch).toHaveBeenCalledWith(`${BASE_URL}/7`, {});
  });

  it('createAsset POSTs JSON', async () => {
    fetch.mockResolvedValue(jsonResponse(201, { id: 1, name: 'X' }));
    await createAsset({ name: 'X' });
    const [url, options] = fetch.mock.calls[0];
    expect(url).toBe(BASE_URL);
    expect(options.method).toBe('POST');
    expect(options.headers['Content-Type']).toBe('application/json');
    expect(JSON.parse(options.body)).toEqual({ name: 'X' });
  });

  it('updateAsset PUTs JSON to /{id}', async () => {
    fetch.mockResolvedValue(jsonResponse(200, { id: 3 }));
    await updateAsset(3, { name: 'Y' });
    const [url, options] = fetch.mock.calls[0];
    expect(url).toBe(`${BASE_URL}/3`);
    expect(options.method).toBe('PUT');
  });

  it('deleteAsset sends DELETE and handles 204 No Content', async () => {
    fetch.mockResolvedValue({ ok: true, status: 204 });
    const result = await deleteAsset(4);
    expect(fetch).toHaveBeenCalledWith(`${BASE_URL}/4`, { method: 'DELETE' });
    expect(result).toBeNull();
  });

  it('throws an error carrying the backend message and status', async () => {
    fetch.mockResolvedValue(jsonResponse(404, { status: 404, message: 'Asset not found with id 9' }));
    await expect(getAssetById(9)).rejects.toMatchObject({ status: 404, message: 'Asset not found with id 9' });
  });

  it('includes validation field errors in the message', async () => {
    fetch.mockResolvedValue(jsonResponse(400, { message: 'Validation failed', fieldErrors: { name: 'Name is required' } }));
    await expect(createAsset({})).rejects.toThrow('Validation failed: Name is required');
  });
});
