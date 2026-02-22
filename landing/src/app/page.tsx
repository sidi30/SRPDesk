import HeroSection from "@/components/landing/HeroSection";
import ProblemSection from "@/components/landing/ProblemSection";
import SolutionSection from "@/components/landing/SolutionSection";
import InteractiveDemo from "@/components/landing/InteractiveDemo";
import FeaturePreview from "@/components/landing/FeaturePreview";
import CrisisTimeline from "@/components/landing/CrisisTimeline";
import CtaSection from "@/components/landing/CtaSection";

export default function HomePage() {
  return (
    <>
      <HeroSection />
      <ProblemSection />
      <SolutionSection />
      <InteractiveDemo />
      <FeaturePreview />
      <CrisisTimeline />
      <CtaSection />
    </>
  );
}
