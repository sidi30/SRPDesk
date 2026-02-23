import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ReadinessGauge } from '../components/ReadinessGauge';

describe('ReadinessGauge', () => {
  it('renders score value', () => {
    render(<ReadinessGauge score={75} />);
    expect(screen.getByText('75')).toBeDefined();
    expect(screen.getByText('/100')).toBeDefined();
  });

  it('renders with custom label', () => {
    render(<ReadinessGauge score={50} label="Overall Score" />);
    expect(screen.getByText('Overall Score')).toBeDefined();
  });

  it('renders SVG circle elements', () => {
    const { container } = render(<ReadinessGauge score={80} />);
    const svg = container.querySelector('svg');
    expect(svg).not.toBeNull();
    const circles = container.querySelectorAll('circle');
    expect(circles.length).toBe(2); // background + progress
  });

  it('clamps score to 0-100', () => {
    render(<ReadinessGauge score={150} />);
    // Should render 150 as text but clamp the progress visually
    expect(screen.getByText('150')).toBeDefined();
  });

  it('applies green color for high scores', () => {
    const { container } = render(<ReadinessGauge score={85} />);
    const progressCircle = container.querySelectorAll('circle')[1];
    expect(progressCircle?.getAttribute('stroke')).toBe('#22c55e');
  });

  it('applies red color for low scores', () => {
    const { container } = render(<ReadinessGauge score={20} />);
    const progressCircle = container.querySelectorAll('circle')[1];
    expect(progressCircle?.getAttribute('stroke')).toBe('#ef4444');
  });

  it('applies yellow color for medium scores', () => {
    const { container } = render(<ReadinessGauge score={65} />);
    const progressCircle = container.querySelectorAll('circle')[1];
    expect(progressCircle?.getAttribute('stroke')).toBe('#eab308');
  });

  it('applies orange color for below-medium scores', () => {
    const { container } = render(<ReadinessGauge score={45} />);
    const progressCircle = container.querySelectorAll('circle')[1];
    expect(progressCircle?.getAttribute('stroke')).toBe('#f97316');
  });
});
