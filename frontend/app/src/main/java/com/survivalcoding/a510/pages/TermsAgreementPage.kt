package com.survivalcoding.a510.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivalcoding.a510.components.TermsAgreementItem
import androidx.compose.foundation.background
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.ui.Alignment

@Composable
fun TermsAgreementPage(
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var allChecked by remember { mutableStateOf(false) }
    var term1Checked by remember { mutableStateOf(false) }
    var term2Checked by remember { mutableStateOf(false) }
    var term3Checked by remember { mutableStateOf(false) }
    var term4Checked by remember { mutableStateOf(false) }
    var term5Checked by remember { mutableStateOf(false) }
    var term6Checked by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "이용약관에 동의해주세요",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 30.dp, bottom = 30.dp)
        )

        AllAgreeCheckbox(
            checked = allChecked,
            onCheckedChange = { checked ->
                allChecked = checked
                term1Checked = checked
                term2Checked = checked
                term3Checked = checked
                term4Checked = checked
                term5Checked = checked
                term6Checked = checked
            },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TermsAgreementItem(
            text = "계정 이용 약관 동의",
            checked = term1Checked,
            onCheckedChange = { term1Checked = it },
            onDetailClick = { }
        )

        TermsAgreementItem(
            text = "계정 개인 정보 수집 이용 동의",
            checked = term2Checked,
            onCheckedChange = { term2Checked = it },
            onDetailClick = { }
        )

        TermsAgreementItem(
            text = "터디퍼디 이용 약관 동의",
            checked = term3Checked,
            onCheckedChange = { term3Checked = it },
            onDetailClick = { }
        )

        TermsAgreementItem(
            text = "터디퍼디 개인 정보 수집 이용 동의",
            checked = term4Checked,
            onCheckedChange = { term4Checked = it },
            onDetailClick = { }
        )

        TermsAgreementItem(
            text = "만 14세 이상입니다.",
            checked = term5Checked,
            onCheckedChange = { term5Checked = it },
            onDetailClick = { }
        )

        TermsAgreementItem(
            text = "광고 수신 동의",
            checked = term6Checked,
            onCheckedChange = { term6Checked = it },
            onDetailClick = { },
            isEssential = false
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (term1Checked && term2Checked && term3Checked &&
                    term4Checked && term5Checked) {
                    onNextClick()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF04C628)
            ),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text(text = "다음")
        }
    }
}

@Composable
private fun AllAgreeCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF5F5F5),  // 연한 회색 배경
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF00C950),
                uncheckedColor = Color.Gray
            ),
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = "모두 동의합니다.",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTermsAgreementPage() {
    TermsAgreementPage(
        onNextClick = { }
    )
}