import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import App from './App';
import * as api from './api/assetApi';

jest.mock('./api/assetApi');

const pageOf = (content, page = 0, totalPages = 3, totalElements = 12) => ({
  content, page, size: 5, totalElements, totalPages, first: page === 0, last: page === totalPages - 1,
});

const macbook = { id: 1, name: 'MacBook Pro 16"', category: 'Laptop', serialNumber: 'SN-90812', assignedTo: 'Mohammed Al-Jarbou', status: 'In Use', purchaseDate: '2024-01-15' };
const dell = { id: 2, name: 'Dell XPS 15', category: 'Laptop', serialNumber: 'SN-33211', assignedTo: 'Eyad Al-Shuhail', status: 'In Use', purchaseDate: '2024-03-10' };
const server = { id: 8, name: 'Dell PowerEdge R750', category: 'Server', serialNumber: 'SN-70031', assignedTo: 'Data Center', status: 'In Use', purchaseDate: '2022-05-30' };

beforeEach(() => {
  jest.resetAllMocks();
  api.getAssets.mockResolvedValue(pageOf([macbook, dell]));
});

describe('IT Asset Management System (connected to the Spring backend)', () => {
  it('renders the navigation menu', async () => {
    render(<App />);
    expect(screen.getByText('IT Asset Management System')).toBeInTheDocument();
    expect(screen.getByText('Asset List')).toBeInTheDocument();
    expect(screen.getByText('Add Asset')).toBeInTheDocument();
    expect(screen.getByText('About Team')).toBeInTheDocument();
    await screen.findByText('Dell XPS 15');
  });

  it('loads the first page of assets from the API on startup', async () => {
    render(<App />);
    expect(await screen.findByText('MacBook Pro 16"')).toBeInTheDocument();
    expect(screen.getByText('Dell XPS 15')).toBeInTheDocument();
    expect(screen.getByText('Page 1 of 3')).toBeInTheDocument();
    expect(screen.getByText('12 assets')).toBeInTheDocument();
    expect(api.getAssets).toHaveBeenCalledWith(0, 5);
  });

  it('requests the next page when Next is clicked', async () => {
    render(<App />);
    await screen.findByText('Dell XPS 15');
    api.getAssets.mockResolvedValueOnce(pageOf([server], 1));

    fireEvent.click(screen.getByText('Next'));

    expect(await screen.findByText('Dell PowerEdge R750')).toBeInTheDocument();
    expect(api.getAssets).toHaveBeenLastCalledWith(1, 5);
    expect(screen.getByText('Page 2 of 3')).toBeInTheDocument();
  });

  it('reloads from page 0 when the page size changes', async () => {
    render(<App />);
    await screen.findByText('Dell XPS 15');

    fireEvent.change(screen.getByLabelText('Page size'), { target: { value: '10' } });

    await waitFor(() => expect(api.getAssets).toHaveBeenLastCalledWith(0, 10));
  });

  it('searches for a single asset by id', async () => {
    api.getAssetById.mockResolvedValue(server);
    render(<App />);
    await screen.findByText('Dell XPS 15');

    fireEvent.change(screen.getByPlaceholderText(/Search by asset ID/i), { target: { value: '8' } });
    fireEvent.click(screen.getByText('Search'));

    expect(await screen.findByText('Dell PowerEdge R750')).toBeInTheDocument();
    expect(screen.queryByText('Dell XPS 15')).not.toBeInTheDocument();
    expect(api.getAssetById).toHaveBeenCalledWith('8');
  });

  it('shows a message when no asset has the searched id', async () => {
    const notFound = Object.assign(new Error('Asset not found with id 99'), { status: 404 });
    api.getAssetById.mockRejectedValue(notFound);
    render(<App />);
    await screen.findByText('Dell XPS 15');

    fireEvent.change(screen.getByPlaceholderText(/Search by asset ID/i), { target: { value: '99' } });
    fireEvent.click(screen.getByText('Search'));

    expect(await screen.findByRole('alert')).toHaveTextContent('No asset found with ID 99');
  });

  it('adds a new asset through the API', async () => {
    api.createAsset.mockResolvedValue({ id: 13 });
    render(<App />);
    await screen.findByText('Dell XPS 15');

    fireEvent.click(screen.getByText('Add Asset'));
    fireEvent.change(screen.getByPlaceholderText('Asset Name'), { target: { value: 'Surface Pro' } });
    fireEvent.change(screen.getByPlaceholderText('Serial Number'), { target: { value: 'SN-12345' } });
    fireEvent.change(screen.getByPlaceholderText('Assigned To'), { target: { value: 'IT Dept' } });
    fireEvent.change(document.querySelector('input[type="date"]'), { target: { value: '2025-01-01' } });
    fireEvent.click(screen.getByText('Save Asset'));

    await waitFor(() => expect(api.createAsset).toHaveBeenCalledWith(expect.objectContaining({
      name: 'Surface Pro', serialNumber: 'SN-12345', assignedTo: 'IT Dept', purchaseDate: '2025-01-01',
    })));
    // back on the list, which is reloaded from the server
    await waitFor(() => expect(api.getAssets).toHaveBeenCalledTimes(2));
  });

  it('updates an existing asset through the API', async () => {
    api.updateAsset.mockResolvedValue({ ...dell, assignedTo: 'Hamad' });
    render(<App />);
    await screen.findByText('Dell XPS 15');

    fireEvent.click(screen.getAllByText('Edit')[1]);
    expect(screen.getByText('Edit Asset')).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('Assigned To'), { target: { value: 'Hamad' } });
    fireEvent.click(screen.getByText('Save Asset'));

    await waitFor(() => expect(api.updateAsset).toHaveBeenCalledWith(2, expect.objectContaining({ assignedTo: 'Hamad' })));
  });

  it('deletes an asset through the API and reloads the list', async () => {
    api.deleteAsset.mockResolvedValue(null);
    render(<App />);
    await screen.findByText('Dell XPS 15');

    fireEvent.click(screen.getAllByText('Delete')[0]);

    await waitFor(() => expect(api.deleteAsset).toHaveBeenCalledWith(1));
    await waitFor(() => expect(api.getAssets).toHaveBeenCalledTimes(2));
  });

  it('shows an error when the backend is unreachable', async () => {
    api.getAssets.mockRejectedValue(new Error('Failed to fetch'));
    render(<App />);
    expect(await screen.findByRole('alert')).toHaveTextContent('Could not load assets: Failed to fetch');
  });

  it('navigates to the About page and shows the team', async () => {
    render(<App />);
    await screen.findByText('Dell XPS 15');
    fireEvent.click(screen.getByText('About Team'));
    expect(screen.getByText('Project Team Members')).toBeInTheDocument();
    expect(screen.getByText('Mohammed Al-Jarbou')).toBeInTheDocument();
    expect(screen.getByText('Eyad Al-Shuhail')).toBeInTheDocument();
  });
});
