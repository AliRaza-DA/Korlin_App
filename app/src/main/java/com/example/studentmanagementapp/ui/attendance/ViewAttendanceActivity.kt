package com.example.studentmanagementapp.ui.attendance

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagementapp.R
import com.example.studentmanagementapp.adapter.AttendanceSessionAdapter
import com.example.studentmanagementapp.adapter.AttendanceSessionItem
import com.example.studentmanagementapp.adapter.CourseSelectAdapter
import com.example.studentmanagementapp.data.entity.Course
import com.example.studentmanagementapp.databinding.ActivityViewAttendanceBinding
import com.example.studentmanagementapp.ui.attendance.AttendanceRecordActivity.Companion.EXTRA_COURSE_CODE
import com.example.studentmanagementapp.ui.attendance.AttendanceRecordActivity.Companion.EXTRA_COURSE_ID
import com.example.studentmanagementapp.ui.attendance.AttendanceRecordActivity.Companion.EXTRA_COURSE_NAME
import com.example.studentmanagementapp.ui.attendance.AttendanceRecordActivity.Companion.EXTRA_INSTRUCTOR
import com.example.studentmanagementapp.ui.attendance.AttendanceRecordActivity.Companion.EXTRA_SESSION_DATE
import com.example.studentmanagementapp.viewmodel.AttendanceViewModel
import com.example.studentmanagementapp.viewmodel.CourseViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ViewAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewAttendanceBinding
    private val attendanceViewModel: AttendanceViewModel by viewModels()
    private val courseViewModel: CourseViewModel by viewModels()
    private var selectedCourse: Course? = null
    private var attendanceLiveData: LiveData<List<com.example.studentmanagementapp.data.entity.Attendance>>? = null
    private lateinit var adapter: AttendanceSessionAdapter
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.view_attendance)

        adapter = AttendanceSessionAdapter(emptyList()) { item ->
            val course = selectedCourse ?: return@AttendanceSessionAdapter
            startActivity(
                android.content.Intent(this, AttendanceRecordActivity::class.java).apply {
                    putExtra(EXTRA_COURSE_ID, course.courseId)
                    putExtra(EXTRA_SESSION_DATE, item.sessionDateMs)
                    putExtra(EXTRA_COURSE_NAME, course.courseName)
                    putExtra(EXTRA_COURSE_CODE, course.courseCode)
                    putExtra(EXTRA_INSTRUCTOR, course.instructorName)
                }
            )
        }
        binding.rvAttendance.layoutManager = LinearLayoutManager(this)
        binding.rvAttendance.adapter = adapter

        courseViewModel.courses.observe(this) { courses ->
            if (courses.isNotEmpty() && selectedCourse == null) {
                showCourseSelectorBottomSheet(courses)
            }
        }

        binding.btnSelectCourse.setOnClickListener {
            courseViewModel.courses.value?.let { showCourseSelectorBottomSheet(it) }
        }
    }

    private fun showCourseSelectorBottomSheet(courses: List<Course>) {
        val dialog = BottomSheetDialog(this, R.style.FullScreenBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_select_course, null)
        view.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
        dialog.setContentView(view)

        val rvCourses = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvCourses)
        rvCourses.layoutManager = LinearLayoutManager(this)

        val adapter = CourseSelectAdapter(courses) { course ->
            selectedCourse = course
            binding.tvSelectedCourse.text = "${course.courseName} (${course.courseCode})"
            supportActionBar?.subtitle = course.instructorName
            observeAttendance()
            dialog.dismiss()
        }

        rvCourses.adapter = adapter
        dialog.show()
    }

    private fun observeAttendance() {
        val courseId = selectedCourse?.courseId ?: return
        attendanceLiveData?.removeObservers(this)
        attendanceLiveData = attendanceViewModel.getAttendance(courseId)
        attendanceLiveData?.observe(this) { records ->
            if (records.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                adapter.submitList(emptyList())
                return@observe
            }

            binding.tvEmptyState.visibility = View.GONE
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val sessions = records.groupBy { normalizeToDay(it.date.time) }
                .entries
                .sortedByDescending { it.key }
                .map { entry ->
                    AttendanceSessionItem(
                        sessionDateMs = entry.key,
                        title = getString(R.string.attendance_session_title),
                        subtitle = formatter.format(entry.key)
                    )
                }
            adapter.submitList(sessions)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun normalizeToDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
