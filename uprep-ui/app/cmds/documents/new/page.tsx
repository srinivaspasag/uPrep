import CmdsUploadForm from "@/components/CmdsUploadForm";

export default function NewDocumentPage() {
  return (
    <CmdsUploadForm
      kind="document"
      title="Add a Document"
      accept=".pdf,.doc,.docx,.ppt,.pptx,.txt,image/*"
      hint="PDF, Word, PowerPoint, image — up to a few MB"
    />
  );
}
