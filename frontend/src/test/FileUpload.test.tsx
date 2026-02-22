import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { FileUpload } from '../components/FileUpload';

describe('FileUpload', () => {
  it('renders upload zone with correct aria-label', () => {
    render(<FileUpload onUpload={vi.fn()} />);
    expect(
      screen.getByLabelText('Zone de téléversement de fichier')
    ).toBeInTheDocument();
  });

  it('renders upload text content', () => {
    render(<FileUpload onUpload={vi.fn()} />);
    expect(screen.getByText(/Drag and drop a file/)).toBeInTheDocument();
    expect(screen.getByText('browse')).toBeInTheDocument();
    expect(screen.getByText('Max 50MB')).toBeInTheDocument();
  });

  it('shows loading state when isLoading=true', () => {
    render(<FileUpload onUpload={vi.fn()} isLoading={true} />);
    expect(screen.getByText('Uploading...')).toBeInTheDocument();
    expect(screen.queryByText(/Drag and drop/)).not.toBeInTheDocument();
  });

  it('calls onUpload when a file is selected via input', () => {
    const onUpload = vi.fn();
    render(<FileUpload onUpload={onUpload} />);

    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    expect(input).toBeTruthy();

    const file = new File(['test content'], 'test-sbom.json', {
      type: 'application/json',
    });
    fireEvent.change(input, { target: { files: [file] } });

    expect(onUpload).toHaveBeenCalledOnce();
    expect(onUpload).toHaveBeenCalledWith(file);
  });

  it('accepts the accept prop on the file input', () => {
    render(<FileUpload onUpload={vi.fn()} accept=".json,.xml" />);
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    expect(input.accept).toBe('.json,.xml');
  });

  it('has role="button" for keyboard accessibility', () => {
    render(<FileUpload onUpload={vi.fn()} />);
    expect(
      screen.getByRole('button', { name: 'Zone de téléversement de fichier' })
    ).toBeInTheDocument();
  });
});
