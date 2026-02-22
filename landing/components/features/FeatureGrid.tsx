import type { ReactNode } from "react";

type FeatureGridProps = {
  children: ReactNode;
};

export default function FeatureGrid({ children }: FeatureGridProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {children}
    </div>
  );
}
