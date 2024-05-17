package com.github.se.eventradar.ui.qrCode

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.github.se.eventradar.R
import com.github.se.eventradar.model.repository.event.MockEventRepository
import com.github.se.eventradar.model.repository.user.MockUserRepository
import com.github.se.eventradar.ui.BottomNavigationMenu
import com.github.se.eventradar.ui.component.EventCategory
import com.github.se.eventradar.ui.component.EventComponentsStyle
import com.github.se.eventradar.ui.component.EventDateTime
import com.github.se.eventradar.ui.component.EventDescription
import com.github.se.eventradar.ui.component.EventDistance
import com.github.se.eventradar.ui.component.EventTitle
import com.github.se.eventradar.ui.component.GoBackButton
import com.github.se.eventradar.ui.component.Logo
import com.github.se.eventradar.ui.navigation.NavigationActions
import com.github.se.eventradar.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.github.se.eventradar.viewmodel.qrCode.QrCodeAnalyser
import com.github.se.eventradar.viewmodel.qrCode.ScanTicketQrViewModel

@Composable
fun QrCodeTicketUi(
    viewModel: ScanTicketQrViewModel = hiltViewModel(),
    navigationActions: NavigationActions
) {

  val qrScanUiState = viewModel.uiState.collectAsStateWithLifecycle()

  val context = LocalContext.current

  ConstraintLayout(
      modifier = Modifier.fillMaxSize().testTag("qrCodeScannerScreen"),
  ) {
    val (logo, tabs, eventDetails, bottomNav) = createRefs()
    Logo(
        modifier =
            Modifier.fillMaxWidth()
                .fillMaxWidth()
                .constrainAs(logo) {
                  top.linkTo(parent.top, margin = 32.dp)
                  start.linkTo(parent.start, margin = 16.dp)
                }
                .testTag("logo"),
    )
    TabRow(
        selectedTabIndex = qrScanUiState.value.tabState.ordinal,
        modifier =
            Modifier.fillMaxWidth()
                .padding(top = 8.dp)
                .constrainAs(tabs) {
                  top.linkTo(logo.bottom, margin = 16.dp)
                  start.linkTo(parent.start)
                  end.linkTo(parent.end)
                }
                .testTag("tabs"),
        contentColor = MaterialTheme.colorScheme.primary) {
          Tab(
              selected = qrScanUiState.value.tabState == ScanTicketQrViewModel.Tab.MyEvent,
              onClick = { viewModel.changeTabState(ScanTicketQrViewModel.Tab.MyEvent) },
              modifier = Modifier.testTag("My Event"),
          ) {
            Text(
                text = "My Event",
                style =
                    TextStyle(
                        fontSize = 19.sp,
                        lineHeight = 17.sp,
                        fontFamily = FontFamily(Font(R.font.roboto)),
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.25.sp,
                    ),
                modifier = Modifier.padding(bottom = 8.dp))
          }
          Tab(
              selected = qrScanUiState.value.tabState == ScanTicketQrViewModel.Tab.ScanQr,
              onClick = {
                viewModel.changeTabState(ScanTicketQrViewModel.Tab.ScanQr)
              }, // selectedTabIndex = 1
              modifier = Modifier.testTag("Scan QR Code")) {
                Text(
                    text = "Scan Ticket",
                    style =
                        TextStyle(
                            fontSize = 19.sp,
                            lineHeight = 17.sp,
                            fontFamily = FontFamily(Font(R.font.roboto)),
                            fontWeight = FontWeight(500),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.25.sp,
                        ),
                    modifier = Modifier.padding(bottom = 8.dp))
              }
        }

    if (qrScanUiState.value.tabState == ScanTicketQrViewModel.Tab.MyEvent) {
      val widthPadding = 34.dp
      val imageHeight = 191.dp

      val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

      val componentStyle =
          EventComponentsStyle(
              MaterialTheme.colorScheme.onSurface,
              MaterialTheme.colorScheme.onSurfaceVariant,
              MaterialTheme.colorScheme.onSurface,
          )

      val (
          image,
          backButton,
          title,
          description,
          distance,
          category,
          dateAndTime,
          ticketSold,
          lazyEventDetails) =
          createRefs()

      // TODO uncomment when image are implemented
      // val imagePainter: Painter = rememberImagePainter(eventUiState.eventPhoto)
      val imagePainter: Painter = rememberImagePainter(R.drawable.placeholderbig)

      LazyColumn(
          modifier =
              Modifier.fillMaxSize()
                  .padding(horizontal = 16.dp)
                  .constrainAs(lazyEventDetails) {
                    top.linkTo(tabs.bottom, margin = 0.dp)
                    start.linkTo(parent.start, margin = 0.dp)
                    end.linkTo(parent.end, margin = 0.dp)
                  }
                  .testTag("lazyEventDetails")) {
            item {
              Image(
                  painter = imagePainter,
                  contentDescription = "Event banner image",
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(imageHeight)
                          .constrainAs(image) {
                            top.linkTo(tabs.bottom, margin = 0.dp)
                            start.linkTo(parent.start, margin = 0.dp)
                          }
                          .testTag("eventImage"),
                  contentScale = ContentScale.FillWidth)
            }
            item {
              GoBackButton(
                  modifier =
                      Modifier.wrapContentSize().constrainAs(backButton) {
                        top.linkTo(image.bottom, margin = 8.dp)
                        start.linkTo(image.start, margin = 4.dp)
                      }) {
                    navigationActions.goBack()
                  }
            }
            item {
              EventTitle(
                  modifier =
                      Modifier.constrainAs(title) {
                        top.linkTo(image.bottom, margin = 32.dp)
                        start.linkTo(image.start)
                        end.linkTo(image.end)
                      },
                  eventUiState = uiState.eventUiState,
                  style = componentStyle)
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item {
              EventDescription(
                  modifier =
                      Modifier
                          // .padding(start = widthPadding, end = widthPadding)
                          .constrainAs(description) {
                            top.linkTo(title.bottom, margin = 32.dp)
                            start.linkTo(parent.start, margin = widthPadding)
                          },
                  eventUiState = uiState.eventUiState,
                  componentStyle)
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item {
              EventDistance(
                  modifier =
                      Modifier.constrainAs(distance) {
                        top.linkTo(description.bottom, margin = 32.dp)
                        start.linkTo(parent.start, margin = widthPadding)
                      },
                  eventUiState = uiState.eventUiState,
                  componentStyle)
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item {
              EventDateTime(
                  modifier =
                      Modifier.constrainAs(dateAndTime) {
                        top.linkTo(distance.bottom, margin = 32.dp)
                        start.linkTo(parent.start, margin = widthPadding)
                      },
                  eventUiState = uiState.eventUiState,
                  componentStyle)
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item {
              EventCategory(
                  modifier =
                      Modifier.constrainAs(category) {
                        top.linkTo(dateAndTime.bottom, margin = 32.dp)
                        start.linkTo(parent.start, margin = widthPadding)
                      },
                  eventUiState = uiState.eventUiState,
                  componentStyle)
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }

            item {
              Column(
                  modifier =
                      Modifier.constrainAs(ticketSold) {
                        top.linkTo(category.bottom, margin = 32.dp)
                        start.linkTo(parent.start, margin = widthPadding)
                      }) {
                    Text(
                        text = "Tickets Sold",
                        style = componentStyle.fieldTitleStyle,
                        color = componentStyle.fieldTitleColor,
                        modifier = Modifier.testTag("ticketSoldTitle"))
                    Text(
                        text =
                            "${uiState.eventUiState.ticket.purchases} tickets sold", // TODO CHNAGE
                                                                                     // TO SOLD
                        style = componentStyle.contentStyle,
                        color = componentStyle.contentColor,
                        modifier = Modifier.testTag("ticketSoldContent"))
                  }
            }
          }
    } else {
      when (qrScanUiState.value.action) {
        ScanTicketQrViewModel.Action.ScanTicket -> {
          Column(modifier = Modifier.testTag("QrScanner")) {
            QrCodeScanner(analyser = viewModel.qrCodeAnalyser)
          }
        }
        ScanTicketQrViewModel.Action.ApproveEntry -> {
          println("ApprovedBox should now be displayed")
          EntryDialog(0, viewModel)
        }
        ScanTicketQrViewModel.Action.DenyEntry -> {
          EntryDialog(1, viewModel)
        }
        ScanTicketQrViewModel.Action.FirebaseUpdateError,
        ScanTicketQrViewModel.Action.FirebaseFetchError,
        ScanTicketQrViewModel.Action.AnalyserError -> {
          EntryDialog(2, viewModel)
        }
      }
    }
    BottomNavigationMenu(
        onTabSelected = { tab ->
          navigationActions.navigateTo(tab)
          viewModel.resetConditions()
        },
        tabList = TOP_LEVEL_DESTINATIONS,
        selectedItem = TOP_LEVEL_DESTINATIONS[3],
        modifier =
            Modifier.testTag("bottomNavMenu").constrainAs(bottomNav) {
              bottom.linkTo(parent.bottom)
              start.linkTo(parent.start)
              end.linkTo(parent.end)
            })
  }
}

@Composable
fun EntryDialog(edr: Int, viewModel: ScanTicketQrViewModel) {
  Dialog(onDismissRequest = { viewModel.changeAction(ScanTicketQrViewModel.Action.ScanTicket) }) {
    val boxColor =
        when (edr) {
          0 -> Color.Green
          1 -> Color.Red
          else -> Color.Yellow
        }

    Box(
        modifier =
            Modifier.size(400.dp) // Adjust the size here to make it larger
                .background(boxColor, RoundedCornerShape(8.dp))
                .padding(20.dp)
                .semantics {
                  testTag =
                      when (boxColor) {
                        Color.Green -> "ApprovedBox"
                        Color.Red -> "DeniedBox"
                        else -> "ErrorBox"
                      }
                },
        // Aligning content to the top right corner
    ) {
      Column(
          modifier = Modifier.fillMaxSize().padding(vertical = 16.dp),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally) {
            val textToShow =
                when (edr) {
                  0 -> "Entry Approved"
                  1 -> "Entry Denied"
                  else -> "Error, Please Retry"
                }
            Text(
                text = textToShow,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                modifier =
                    Modifier.semantics {
                      testTag =
                          when (edr) {
                            0 -> "EntryApprovedText"
                            1 -> "EntryDeniedText"
                            else -> "ErrorText"
                          }
                    })
          }
      Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
        IconButton(
            onClick = { viewModel.changeAction(ScanTicketQrViewModel.Action.ScanTicket) },
            modifier =
                Modifier.semantics { testTag = "closeButton" } // Adding testTag to the IconButton
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewQrCodeTicketUi() {
  // Create a mock NavigationActions to pass into the function
  val userRepository = MockUserRepository()
  (userRepository as MockUserRepository).updateCurrentUserId("user1")
  val eventRepository = MockEventRepository()
  val qrCodeAnalyser = QrCodeAnalyser()
  val viewModel = ScanTicketQrViewModel(userRepository, eventRepository, qrCodeAnalyser, "1")
  QrCodeTicketUi(viewModel, NavigationActions(rememberNavController()))
}

@Preview(showBackground = true)
@Composable
fun PreviewQrCodeTicketGranted() {
  // Create a mock NavigationActions to pass into the function
  val userRepository = MockUserRepository()
  (userRepository as MockUserRepository).updateCurrentUserId("user1")
  val eventRepository = MockEventRepository()
  val qrCodeAnalyser = QrCodeAnalyser()
  val viewModel = ScanTicketQrViewModel(userRepository, eventRepository, qrCodeAnalyser, "1")
  viewModel.changeTabState(ScanTicketQrViewModel.Tab.ScanQr)
  viewModel.changeAction(ScanTicketQrViewModel.Action.ApproveEntry)
  QrCodeTicketUi(viewModel, NavigationActions(rememberNavController()))
}
//// s
// @Preview(showBackground = true)
// @Composable
// fun PreviewQrCodeTicketDeny() {
//  // Create a mock NavigationActions to pass into the function
//  val userRepository = MockUserRepository()
//  (userRepository as MockUserRepository).updateCurrentUserId("user1")
//  val eventRepository = MockEventRepository()
//  val qrCodeAnalyser = QrCodeAnalyser()
//  val viewModel = ScanTicketQrViewModel(userRepository, eventRepository, qrCodeAnalyser)
//  viewModel.changeAction(ScanTicketQrViewModel.Action.DenyEntry)
//  QrCodeTicketUi(viewModel, NavigationActions(rememberNavController()))
// }
//
// @Preview(showBackground = true)
// @Composable
// fun PreviewQrCodeTicketError() {
//  // Create a mock NavigationActions to pass into the function
//  val userRepository = MockUserRepository()
//  (userRepository as MockUserRepository).updateCurrentUserId("user1")
//  val eventRepository = MockEventRepository()
//  val qrCodeAnalyser = QrCodeAnalyser()
//  val viewModel = ScanTicketQrViewModel(userRepository, eventRepository, qrCodeAnalyser)
//  viewModel.changeAction(ScanTicketQrViewModel.Action.FirebaseUpdateError)
//  QrCodeTicketUi(viewModel, NavigationActions(rememberNavController()))
// }
