package fr.tunaki.stackoverflow.burnaki.bot.command;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiPredicate;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.tunaki.stackoverflow.burnaki.BurninationManager;
import fr.tunaki.stackoverflow.burnaki.bot.Burnaki;
import fr.tunaki.stackoverflow.burnaki.entity.BurninationProgress;
import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

@Component
public class GetProgressCommand implements Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(GetProgressCommand.class);

	private BurninationManager burninationManager;

	@Autowired
	public GetProgressCommand(BurninationManager burninationManager) {
		this.burninationManager = burninationManager;
	}

	@Override
	public String getName() {
		return "get progress";
	}

	@Override
	public String getDescription() {
		return "Prints the current progress of the tag's burnination. The tag can be omitted if ran inside the dedicated burn room.";
	}

	@Override
	public String getUsage() {
		return "get progress [tag]";
	}

	@Override
	public BiPredicate<String, String> matches() {
		return String::startsWith;
	}

	@Override
	public int argumentCount() {
		return 1;
	}

	@Override
	public void execute(Message message, Room room, Burnaki burnaki, String[] arguments) {
		String tag = arguments[0];
		List<BurninationProgress> progresses;
		try {
			progresses = burninationManager.getProgress(tag);
		} catch (Exception e) {
			LOGGER.error("Cannot get progress of burnination for tag [{}]", tag, e);
			room.replyTo(message.getId(), "Cannot get progress of burnination for tag \\[" + tag + "\\]: " + e.getMessage());
			return;
		}
		if (progresses.isEmpty()) {
			room.replyTo(message.getId(), "No registered progress for \\[" + tag + "\\] yet. You need to work some more!");
			return;
		}
		BurninationProgress latest = progresses.get(progresses.size() - 1);
		room.send("Here's a recap of the efforts so far for \\[" + tag + "\\]: Total questions (" + latest.getTotalQuestions() + "), Retagged (" + latest.getRetagged() + "), Closed (" + latest.getClosed() + "), Roombad (" + latest.getRoombad() + "), Manually deleted (" + latest.getManuallyDeleted() + ").");

		room.send("Here are some nice images...");
		List<Date> xClosedData = new ArrayList<>();
		List<Integer> yClosedData = new ArrayList<>();
		for (BurninationProgress progress : progresses) {
			xClosedData.add(Date.from(progress.getId().getProgressDate()));
			yClosedData.add(progress.getClosed());
		}

		XYChart chart = new XYChartBuilder().width(500).height(400).title("Burnination progress").xAxisTitle("Time").yAxisTitle("Number of questions").build();
		chart.getStyler().setChartBackgroundColor(Color.WHITE);
		chart.getStyler().setDatePattern("dd/MM").setMarkerSize(4).setPlotGridVerticalLinesVisible(false);
		chart.addSeries("Closed", xClosedData, yClosedData).setMarker(SeriesMarkers.CIRCLE).setMarkerColor(Color.ORANGE).setLineWidth(0.5f);

		Path path;
		InputStream is;
		try {
			path = Files.createTempFile("burnaki", ".png");
			BitmapEncoder.saveBitmapWithDPI(chart, path.toFile().getAbsolutePath(), BitmapFormat.PNG, 300);
			is = Files.newInputStream(path, StandardOpenOption.DELETE_ON_CLOSE);
		} catch (IOException e) {
			LOGGER.error("Error while computing the progress graph :(", e);
			room.send("Error while computing the progress graph :(, issue was " + e.getMessage());
			return;
		}
		room.uploadImage(path.getFileName().toString(), is).whenComplete((url, t) -> {
			try {
				is.close();
			} catch (IOException e) { }
			if (t == null) {
				room.send(url);
			}
		});
	}

}
