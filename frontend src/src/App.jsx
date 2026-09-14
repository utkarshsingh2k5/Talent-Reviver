import Home from "./home/home.jsx";
import Login from "./login/login.jsx";
import HRDashboard from "./hr/HRDashboard.jsx";
import CreateJob from "./hr/CreateJob.jsx";
import CandidateRankings from "./hr/CandidateRankings.jsx";
import OutreachTracker from "./hr/OutreachTracker.jsx";
import CandidateList from "./hr/CandidateList.jsx";
import "./App.css"
import { ToastContainer } from "react-toastify";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { useContext } from "react";
import { usercontext } from "./appcontext.jsx";
import Forgotpassword from "./resetpassword/resetpassword.jsx";
import Uploadpage from "./upload/upload.jsx";
import Analyse from "./analyse/analyse.jsx";
import Styles from "./loadstyle.module.css"

function App() {

  const { isauthenticated } = useContext(usercontext)
  return (isauthenticated ?
    <>
      <ToastContainer theme="dark" stacked autoClose={1500} />
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/forgotpassword" element={<Forgotpassword />} />
          <Route path="/uploaddoc" element={<Uploadpage />} />
          <Route path="/analysereport" element={<Analyse />} />
          <Route path="/hr/dashboard" element={<HRDashboard />} />
          <Route path="/hr/create-job" element={<CreateJob />} />
          <Route path="/hr/rankings/:jobId" element={<CandidateRankings />} />
          <Route path="/hr/outreach/:jobId" element={<OutreachTracker />} />
          <Route path="/hr/candidates" element={<CandidateList />} />
        </Routes>
      </BrowserRouter>
    </> :
    <div className={Styles.loadani} id="animate">
      <div className={Styles.loadanimation}>
        <div className={Styles.capstart}></div>
        <div className={Styles.loadblock}></div>
      </div>
    </div>

  )
}

export default App
