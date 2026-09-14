import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { usercontext } from '../appcontext';
import Styles from './candidateList.module.css';
import { toast } from 'react-toastify';

function CandidateList() {
    const { backendURL } = useContext(usercontext);
    const navigate = useNavigate();
    const [candidates, setCandidates] = useState([]);
    const [activeJobs, setActiveJobs] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            setIsLoading(true);
            try {
                const [candRes, jobsRes] = await Promise.all([
                    fetch(`${backendURL}/hr/candidates`, { credentials: 'include' }),
                    fetch(`${backendURL}/hr/jobs`, { credentials: 'include' })
                ]);

                if (candRes.ok) setCandidates(await candRes.json());
                if (jobsRes.ok) {
                    const jobs = await jobsRes.json();
                    setActiveJobs(jobs.filter(j => j.status === 'ACTIVE'));
                }
            } catch (error) {
                console.error('Failed to fetch data', error);
            } finally {
                setIsLoading(false);
            }
        };
        fetchData();
    }, [backendURL]);

    const handleQuickContact = async (email, jobId) => {
        try {
            const response = await fetch(`${backendURL}/hr/jobs/${jobId}/outreach`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ candidateEmails: [email] }),
                credentials: 'include'
            });
            if (response.ok) {
                toast.success(`Outreach sent to ${email}!`);
            } else {
                const err = await response.text();
                toast.error(`Contact failed: ${err}`);
            }
        } catch (error) {
            toast.error('Network error occurred');
        }
    };

    return (
        <div className={Styles.container}>
            <div className={Styles.header}>
                <button className={Styles.backBtn} onClick={() => navigate('/hr/dashboard')}>← Back to Dashboard</button>
                <h1>All Candidates Pool</h1>
            </div>

            {isLoading ? (
                <div className={Styles.loader}>Loading candidates...</div>
            ) : (
                <div className={Styles.tableWrapper}>
                    <table className={Styles.candidateTable}>
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Experience</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {candidates.length > 0 ? candidates.map((c, index) => (
                                <tr key={c.email}>
                                    <td>{c.name}</td>
                                    <td>{c.email}</td>
                                    <td>{c.experienceYears} yrs</td>
                                    <td><span className={Styles.statusBadge}>{c.status}</span></td>
                                    <td>
                                        <div style={{ display: 'flex', gap: '8px' }}>
                                            <button className={Styles.viewBtn} onClick={() => navigate(`/hr/rankings/search?email=${c.email}`)}>
                                                Check Matches
                                            </button>
                                            <select
                                                onChange={(e) => handleQuickContact(c.email, e.target.value)}
                                                style={{ padding: '5px', borderRadius: '4px', fontSize: '0.8rem' }}
                                            >
                                                <option value="">Quick Contact...</option>
                                                {activeJobs.map(job => (
                                                    <option key={job.id} value={job.id}>{job.title}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </td>
                                </tr>
                            )) : (
                                <tr>
                                    <td colSpan="5" className={Styles.noData}>No candidates found in the pool.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

export default CandidateList;
