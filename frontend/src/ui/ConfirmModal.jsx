import Modal from "./Modal";
import Button from "./Button";

export default function ConfirmModal({
  title = "Confirm Action",
  description = "Are you sure you want to proceed?",
  confirmText = "Confirm",
  cancelText = "Cancel",
  onConfirm,
  onCancel,
  loading = false
}) {
  return (
    <Modal title={title} onClose={onCancel} width="max-w-md">
      <p className="text-gray-400 mb-10">
        {description}
      </p>

      <div className="flex justify-end gap-4">
        <Button
          variant="secondary"
          onClick={onCancel}
        >
          {cancelText}
        </Button>

        <Button
          variant="danger"
          onClick={onConfirm}
          disabled={loading}
        >
          {loading ? "Processing..." : confirmText}
        </Button>
      </div>
    </Modal>
  );
}