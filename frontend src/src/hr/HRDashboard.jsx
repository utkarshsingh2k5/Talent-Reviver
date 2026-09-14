import React, { useState, useEffect, useContext } from 'react';
import Styles from './hrDashboard.module.css';
import { usercontext } from '../appcontext';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

function HRDashboard() {
    const { username, backendURL } = useContext(usercontext);
    const navigate = useNavigate();
    const [jobs, setJobs] = useState([]);
    const [stats, setStats] = useState({
        activeJobs: 0,
        totalCandidates: 0,
        aiMatchesGenerated: 0,
        highMatches: 0,
        contacted: 0,
        interested: 0,
        notInterested: 0,
        noResponse: 0
    });
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const loadDashboardData = async () => {
            setIsLoading(true);
            try {
                const jobsRes = await fetch(`${backendURL}/hr/jobs`, { credentials: 'include' });
                if (!jobsRes.ok) throw new Error(`Jobs fetch failed: ${jobsRes.status}`);
                const jobsData = await jobsRes.json();
                setJobs(jobsData);

                const statsRes = await fetch(`${backendURL}/hr/dashboard`, { credentials: 'include' });
                if (!statsRes.ok) throw new Error(`Stats fetch failed: ${statsRes.status}`);
                const statsData = await statsRes.json();
                setStats(statsData);
            } catch (error) {
                console.error("Dashboard Load Error:", error);
                toast.error("Backend is offline or unreachable. Please ensure your Spring Boot server is running on port 8080.");
            } finally {
                setIsLoading(false);
            }
        };

        loadDashboardData();
    }, [backendURL]);

    const deleteJob = async (id) => {
        if (!window.confirm("Are you sure you want to delete this job posting?")) return;
        try {
            const res = await fetch(`${backendURL}/hr/jobs/${id}`, {
                method: 'DELETE',
                credentials: 'include'
            });
            if (res.ok) {
                toast.success("Job deleted successfully");
                // Refresh data
                const jobsRes = await fetch(`${backendURL}/hr/jobs`, { credentials: 'include' });
                const jobsData = await jobsRes.json();
                setJobs(jobsData);
            } else {
                toast.error("Failed to delete job");
            }
        } catch (error) {
            toast.error("Network error while deleting job");
        }
    };

    return (
        <div className={Styles.container}>
            <div className={Styles.header}>
                <h1>HR Management Dashboard</h1>
                <p>Welcome back, {username}! Here is your recruitment overview.</p>
            </div>

            <div className={Styles.grid}>
                <div className={Styles.card}>
                    <h3>Active Job Postings</h3>
                    <p className={Styles.stat}>{jobs.filter(j => j.status === 'ACTIVE').length}</p>
                    <button className={Styles.btn} onClick={() => navigate('/hr/create-job')}>Manage Jobs</button>
                </div>
                <div className={Styles.card}>
                    <h3>Total Candidates</h3>
                    <p className={Styles.stat}>{stats.totalCandidates}</p>
                    <button className={Styles.btn} onClick={() => navigate('/hr/candidates')}>View Pool</button>
                </div>
                <div className={Styles.card}>
                    <h3>AI Matched Profiles</h3>
                    <p className={Styles.stat}>{stats.aiMatchesGenerated}</p>
                    <button className={Styles.btn} onClick={() => navigate('/hr/candidates')}>View All Matches</button>
                </div>
                <div className={Styles.card}>
                    <h3>High Priority Matches</h3>
                    <p className={Styles.stat}>{stats.highMatches}</p>
                    <button className={Styles.btn} onClick={() => navigate('/hr/candidates')}>Focus Here</button>
                </div>
            </div>

            <div className={Styles.responseSection}>
                <div className={Styles.sectionHeader}>
                    <h2>Outreach Performance</h2>
                    <button className={Styles.secondaryBtn} onClick={() => navigate('/hr/outreach/all')}>View All Logs</button>
                </div>
                <div className={Styles.responseGrid}>
                    <div className={Styles.responseCard}>
                        <span>Total Contacted</span>
                        <p className={Styles.stat}>{stats.contacted}</p>
                    </div>
                    <div className={Styles.responseCard}>
                        <span>Interested</span>
                        <p className={Styles.stat}>{stats.interested}</p>
                    </div>
                    <div className={Styles.responseCard}>
                        <span>Not Interested</span>
                        <p className={Styles.stat}>{stats.notInterested}</p>
                    </div>
                    <div className={Styles.responseCard}>
                        <span>No Response</span>
                        <p className={Styles.stat}>{stats.noResponse}</p>
                    </div>
                </div>
            </div>

            <div className={Styles.jobsSection}>
                <div className={Styles.sectionHeader}>
                    <h2>Recent Job Postings</h2>
                    <button className={Styles.secondaryBtn} onClick={() => navigate('/hr/create-job')}>+ New Posting</button>
                </div>
                {isLoading ? (
                    <p>Loading jobs...</p>
                ) : jobs.length > 0 ? (
                    <div className={Styles.jobsGrid}>
                        {jobs.map(job => (
                            <div key={job.id} className={Styles.jobCard}>
                                <div className={Styles.jobHeader}>
                                    <h3>{job.title}</h3>
                                    <div className={Styles.badgeGroup}>
                                        <span className={Styles.statusBadge}>{job.status}</span>
                                        <button className={Styles.deleteJobBtn} onClick={() => deleteJob(job.id)}>Delete</button>
                                    </div>
                                </div>
                                <p>{job.location} | {job.employmentType}</p>
                                <div className={Styles.jobActions}>
                                    <button className={Styles.viewRankBtn} onClick={() => navigate(`/hr/rankings/${job.id}`)}>
                                        View Match Rankings
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <p>No jobs created yet.</p>
                )}
            </div>
        </div>
    );
}

export default HRDashboard;
