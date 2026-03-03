import { useEffect, useMemo, useState, useCallback } from "react";
import api from "../api";

export default function AdminUsers() {

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [search, setSearch] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState("ALL");

  /* ================= SEARCH DEBOUNCE ================= */

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search.trim());
    }, 400);
    return () => clearTimeout(timer);
  }, [search]);

  useEffect(() => {
    setPage(0);
  }, [debouncedSearch, roleFilter]);

  /* ================= LOAD USERS ================= */

  const loadUsers = useCallback(async () => {
    try {
      setLoading(true);

      const res = await api.get(
        `/admin/users?page=${page}&size=10`
      );

      const content = Array.isArray(res.data?.content)
        ? res.data.content
        : [];

      setUsers(content);
      setTotalPages(res.data?.totalPages ?? 0);

    } catch (err) {
      console.error("Users load error:", err);
      setUsers([]);
      setTotalPages(0);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  /* ================= FILTER ================= */

  const filteredUsers = useMemo(() => {
    return users
      .filter((u) =>
        roleFilter === "ALL" || u.role === roleFilter
      )
      .filter((u) =>
        debouncedSearch === "" ||
        `${u.name || ""} ${u.email || ""}`
          .toLowerCase()
          .includes(debouncedSearch.toLowerCase())
      );
  }, [users, debouncedSearch, roleFilter]);

  /* ================= LOADING ================= */

  if (loading) {
    return (
      <div className="flex items-center justify-center py-40">
        <div className="w-10 h-10 border-4 border-white/10 border-t-brand-gold rounded-full animate-spin" />
      </div>
    );
  }

  /* ================= UI ================= */

  return (
    <div className="space-y-16">

      {/* HEADER */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">

        <div>
          <h2 className="text-3xl font-serif text-brand-gold">
            User Registry
          </h2>
          <p className="text-gray-400 mt-2">
            Registered pilgrims and administrators across Sarathi.
          </p>
        </div>

        <div className="flex gap-4 flex-wrap">

          <input
            type="text"
            placeholder="Search name or email..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="bg-black/50 border border-white/10 rounded-xl px-4 py-2 focus:outline-none focus:border-brand-gold transition"
          />

          <select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            className="bg-black/50 border border-white/10 rounded-xl px-4 py-2 focus:outline-none focus:border-brand-gold transition"
          >
            <option value="ALL">All Roles</option>
            <option value="ROLE_ADMIN">Admins</option>
            <option value="ROLE_USER">Users</option>
          </select>

        </div>

      </div>

      {/* CONTENT */}
      {users.length === 0 ? (

        <div className="card-elevated p-12 text-center">
          <h4 className="text-lg font-semibold mb-2">
            No Registered Users
          </h4>
          <p className="text-gray-400">
            New travelers will appear here once they create an account.
          </p>
        </div>

      ) : filteredUsers.length === 0 ? (

        <div className="card-elevated p-12 text-center">
          <h4 className="text-lg font-semibold mb-2">
            No Matching Results
          </h4>
          <p className="text-gray-400">
            Adjust your search query or role filter.
          </p>
        </div>

      ) : (

        <div className="card-elevated p-8 overflow-x-auto">

          <table className="w-full text-sm">

            <thead className="text-left text-gray-400 border-b border-white/10 uppercase tracking-wider text-xs">
              <tr>
                <th className="py-3">ID</th>
                <th>User</th>
                <th>Role</th>
                <th>Bookings</th>
              </tr>
            </thead>

            <tbody>
              {filteredUsers.map((u) => (
                <tr
                  key={u.id ?? `${u.email}-${u.name}`}
                  className="border-b border-white/5 hover:bg-white/5 transition-all duration-300 transition"
                >

                  <td className="py-4">
                    {u.id ?? "—"}
                  </td>

                  <td>
                    <div className="flex items-center gap-4">

                      <div className="w-10 h-10 rounded-full bg-brand-gold text-black flex items-center justify-center font-semibold">
                        {(u.name?.charAt(0) || "U").toUpperCase()}
                      </div>

                      <div>
                        <div className="font-medium">
                          {u.name || "Unnamed"}
                        </div>
                        <div className="text-xs text-gray-400">
                          {u.email || "—"}
                        </div>
                      </div>

                    </div>
                  </td>

                  <td>
                    <span
                      className={`px-3 py-1 rounded-full text-xs font-medium ${
                        u.role === "ROLE_ADMIN"
                          ? "bg-brand-gold text-black"
                          : "bg-white/10 text-gray-300"
                      }`}
                    >
                      {u.role === "ROLE_ADMIN"
                        ? "Administrator"
                        : "User"}
                    </span>
                  </td>

                  <td>
                    <span className="text-brand-gold font-medium">
                      {u.bookingCount ?? 0}
                    </span>
                  </td>

                </tr>
              ))}
            </tbody>

          </table>

        </div>

      )}

      {/* PAGINATION */}
      {totalPages > 1 && (
        <div className="flex justify-center items-center gap-6 pt-4">

          <button
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
            className="px-6 py-2 rounded-full border border-white/20 text-sm hover:border-brand-gold transition-all duration-300"
          >
            Previous
          </button>

          <span className="text-gray-400 text-sm">
            Page {page + 1} of {totalPages}
          </span>

          <button
            disabled={page + 1 >= totalPages}
            onClick={() => setPage((p) => p + 1)}
            className="px-4 py-2 rounded-full border border-white/20 text-sm disabled:opacity-40 hover:border-brand-gold transition"
          >
            Next
          </button>

        </div>
      )}

    </div>
  );
}