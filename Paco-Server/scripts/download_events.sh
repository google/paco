#!/bin/bash

#appcfg.py download_data --config_file=bulkloader_event.yaml --filename=events.csv --kind=Event --url=http://quantifiedself.appspot.com/remote_api
appcfg.py download_data --config_file=paco_event_config_for_stats.yml --filename=stats_events.csv --kind=Event --url=https://quantifiedself.appspot.com/remote_api


# continuation version of downloader, with larger batchsize to increase throughput
#appcfg.py download_data --config_file=paco_event_config_for_stats.yml --filename=stats_events.csv --db_filename=bulkloader-progress-20160302.160405.sql3 --kind=Event --url=https://quantifiedself.appspot.com/remote_api --result_db_filename=bulkloader-results-20160302.160405.sql3 --batch_size=50
