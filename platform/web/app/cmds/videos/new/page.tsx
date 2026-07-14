import CmdsUploadForm from "@/components/CmdsUploadForm";

export default function NewVideoPage() {
  return (
    <CmdsUploadForm
      kind="video"
      title="Add a Video"
      accept="video/mp4,video/webm,video/*"
      hint="MP4 or WebM — plays inline in the learn app"
    />
  );
}
