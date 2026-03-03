import Skeleton from "./Skeleton";

export default function SkeletonPage() {
  return (
    <div className="min-h-screen px-6 pt-32 space-y-10">
      <Skeleton className="h-12 w-64" />
      <Skeleton className="h-64 w-full" />
      <Skeleton className="h-64 w-full" />
    </div>
  );
}